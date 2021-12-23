package com.google.cloud.pso.bq_security_classifier.functions.dispatcher;

import com.google.cloud.pso.bq_security_classifier.helpers.LoggingHelper;
import com.google.cloud.pso.bq_security_classifier.services.BigQueryService;
import com.google.cloud.pso.bq_security_classifier.services.CloudTasksService;
import com.google.cloud.tasks.v2.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.cloud.pso.bq_security_classifier.helpers.Utils;
import com.google.protobuf.ByteString;

public class DispatcherHelper {

    private final LoggingHelper logger;

    // core components
    private BigQueryService bqService;
    private CloudTasksService cloudTasksService;

    // attributes initialized by user.
    private String solutionProjectId;
    private String solutionRegionId;
    private String runId;
    private String queueId;
    private String httpEndPoint;
    private String serviceAccountEmail;

    // bq scan scope
    private List<String> projectIncludeList;
    private List<String> datasetIncludeList;
    private List<String> datasetExcludeList;
    private List<String> tableIncludeList;
    private List<String> tableExcludeList;

    //outputs
    // list of log messages to be shown to end user
    private List<String> outputMessages;
    // list of tables (project.dataset.table) for which an inspection cloud task was submitted
    private List<String> dispatchedInspectTableTasks;

    public DispatcherHelper(BigQueryService bqService, CloudTasksService cloudTasksService, LoggingHelper logger, String solutionProjectId, String solutionRegionId, String runId, String queueId, String httpEndPoint, String serviceAccountEmail, List<String> projectIncludeList, List<String> datasetIncludeList, List<String> datasetExcludeList, List<String> tableIncludeList, List<String> tableExcludeList) throws IOException {

        this.bqService = bqService;
        this.cloudTasksService = cloudTasksService;

        this.logger = logger;
        this.solutionProjectId = solutionProjectId;
        this.solutionRegionId = solutionRegionId;
        this.runId = runId;
        this.queueId = queueId;
        this.httpEndPoint = httpEndPoint;
        this.serviceAccountEmail = serviceAccountEmail;
        this.projectIncludeList = projectIncludeList;
        this.datasetIncludeList = datasetIncludeList;
        this.datasetExcludeList = datasetExcludeList;
        this.tableIncludeList = tableIncludeList;
        this.tableExcludeList = tableExcludeList;

        initialize();
    }

    private void initialize() throws IOException {

        // Construct the fully qualified Cloud Tasks queue name.
        outputMessages = new ArrayList<>();
        dispatchedInspectTableTasks = new ArrayList<>();
    }

    public List<String> execute() throws IOException {

        logger.logInfoWithTracker(runId, String.format("will scan based on the given scope %s", getBqScanScopeAsStr()));

        if (!tableIncludeList.isEmpty()) {
            processTables(tableIncludeList, tableExcludeList);
            return dispatchedInspectTableTasks;
        }
        if (!datasetIncludeList.isEmpty()) {
            processDatasets(datasetIncludeList, datasetExcludeList, tableExcludeList);
            return dispatchedInspectTableTasks;
        }
        if (!projectIncludeList.isEmpty()) {
            processProjects();
        }

        return dispatchedInspectTableTasks;
    }

    public void processTables(List<String> tableIncludeList,
                              List<String> tableExcludeList) {
        for (String table : tableIncludeList) {
            if (!tableExcludeList.contains(table)) {

                List<String> tokens = Utils.tokenize(table, ".", true);
                String projectId = tokens.get(0);
                String datasetId = tokens.get(1);
                String tableId = tokens.get(2);

                String tableTracker = String.format("%s-%s-%s",
                        projectId,
                        datasetId,
                        tableId);

                String trackingId = String.format("%s-T-%s", runId, tableTracker);

                InspectorTask taskOptions = new InspectorTask();
                taskOptions.setTargetTableProject(projectId);
                taskOptions.setTargetTableDataset(datasetId);
                taskOptions.setTargetTable(tableId);
                taskOptions.setQueueId(queueId);
                taskOptions.setHttpEndPoint(httpEndPoint);
                taskOptions.setTrackingId(trackingId);

                Task task = createInspectorTask(taskOptions);

                dispatchedInspectTableTasks.add(table);

                String logMsg = String.format("Cloud task created with id %s for tracker %s",
                        task.getName(),
                        trackingId);

                outputMessages.add(logMsg);
                logger.logInfoWithTracker(trackingId, logMsg);
            }
        }
    }

    public void processDatasets(List<String> datasetIncludeList,
                                List<String> datasetExcludeList,
                                List<String> tableExcludeList) throws IOException {

        for (String dataset : datasetIncludeList) {
            if (!datasetExcludeList.contains(dataset)) {

                List<String> tokens = Utils.tokenize(dataset, ".", true);
                String projectId = tokens.get(0);
                String datasetId = tokens.get(1);

                String datasetLocation = bqService.getDatasetLocation(projectId, datasetId);

                /*
                 TODO: Support tagging in multiple locations

                 to support all locations:
                 1- Taxonomies/PolicyTags have to be created in each required location
                 2- Update the Tagger Cloud Function to read one mapping per location

                 For now, we don't submit tasks for tables in other locations than the PolicyTag location
                 */
                if (!datasetLocation.equals(solutionRegionId)) {
                    logger.logWarnWithTracker(runId,
                            String.format(
                                    "Ignoring dataset %s in location %s. Only location %s is configured",
                                    dataset,
                                    datasetLocation,
                                    solutionRegionId)
                    );
                    continue;
                }

                List<String> tablesIncludeList = bqService.listTables(projectId, datasetId)
                        .map(t -> String.format("%s.%s.%s", t.getProject(), t.getDataset(), t.getTable()))
                        .collect(Collectors.toList());

                if (tablesIncludeList.isEmpty()) {
                    String msg = String.format(
                            "No tables found under dataset '%s'. Dataset might be empty or no permissions to list tables.",
                            dataset);

                    outputMessages.add(msg);
                    logger.logWarnWithTracker(getRunId(), msg);
                }

                processTables(tablesIncludeList, tableExcludeList);
            }
        }
    }

    public void processProjects() throws IOException {
        logger.logInfoWithTracker(runId, String.format("Will process projects %s", projectIncludeList));

        for (String project : projectIncludeList) {

            logger.logInfoWithTracker(runId, String.format("Inspecting project %s", project));

            // construct a list of datasets in the format project.dataset
            List<String> datasetIncludeList = bqService.listDatasets(project)
                    .map(d -> String.format("%s.%s", d.getProject(), d.getDataset()))
                    .collect(Collectors.toList());

            logger.logInfoWithTracker(runId, String.format("Datasets found in project %s datasets %s", project, datasetIncludeList));

            if (datasetIncludeList.isEmpty()) {
                String msg = String.format(
                        "No datasets found under project '%s'. Project might be empty or no permissions to list datasets.",
                        project);

                outputMessages.add(msg);

                logger.logWarnWithTracker(getRunId(), msg);
            }

            processDatasets(datasetIncludeList, datasetExcludeList, tableExcludeList);
        }
    }

    public Task createInspectorTask(InspectorTask inspectorTask) {

        String payloadTemplate = "{\n" +
                "   \"inputProjectId\":\"%s\",\n" +
                "   \"inputDatasetId\":\"%s\",\n" +
                "   \"inputTableId\":\"%s\",\n" +
                "   \"trackingId\":\"%s\"\n" +
                "}";

        String payload = String.format(payloadTemplate,
                inspectorTask.getTargetTableProject(),
                inspectorTask.getTargetTableDataset(),
                inspectorTask.getTargetTable(),
                inspectorTask.getTrackingId()
        );

        // Construct the fully qualified queue name.
        String queuePath = QueueName.of(solutionProjectId,
                solutionRegionId,
                inspectorTask.getQueueId())
                .toString();

        OidcToken oidcToken = OidcToken.newBuilder()
                .setServiceAccountEmail(serviceAccountEmail)
                .build();

        // Construct the task body.
        Task taskRequest =
                Task.newBuilder()
                        .setHttpRequest(
                                HttpRequest.newBuilder()
                                        .setBody(ByteString.copyFrom(payload, Charset.defaultCharset()))
                                        .setUrl(inspectorTask.getHttpEndPoint())
                                        .setHttpMethod(HttpMethod.POST)
                                        .setOidcToken(oidcToken)
                                        .build()).build();

        return cloudTasksService.submitTask(taskRequest, queuePath);
    }

    public String getSolutionProjectId() {
        return solutionProjectId;
    }

    public String getSolutionRegionId() {
        return solutionRegionId;
    }

    public String getRunId() {
        return runId;
    }

    public String getQueueId() {
        return queueId;
    }

    public String getHttpEndPoint() {
        return httpEndPoint;
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public List<String> getProjectIncludeList() {
        return projectIncludeList;
    }

    public List<String> getDatasetIncludeList() {
        return datasetIncludeList;
    }

    public List<String> getDatasetExcludeList() {
        return datasetExcludeList;
    }

    public List<String> getTableIncludeList() {
        return tableIncludeList;
    }

    public List<String> getTableExcludeList() {
        return tableExcludeList;
    }

    public List<String> getOutputMessages() {
        return outputMessages;
    }


    public void setBigQueryScanScope(List<String> projectIncludeList,
                                     List<String> datasetIncludeList,
                                     List<String> datasetExcludeList,
                                     List<String> tableIncludeList,
                                     List<String> tableExcludeList) {

        this.projectIncludeList = projectIncludeList;
        this.datasetIncludeList = datasetIncludeList;
        this.datasetExcludeList = datasetExcludeList;
        this.tableIncludeList = tableIncludeList;
        this.tableExcludeList = tableExcludeList;
    }

    public String getBqScanScopeAsStr() {
        return "BQ scan scope{" +
                "projectIncludeList=" + projectIncludeList +
                ", datasetIncludeList=" + datasetIncludeList +
                ", datasetExcludeList=" + datasetExcludeList +
                ", tableIncludeList=" + tableIncludeList +
                ", tableExcludeList=" + tableExcludeList +
                '}';
    }
}
