package com.google.cloud.pso.bq_security_classifier.functions.inspector;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.pso.bq_security_classifier.helpers.LoggingHelper;
import com.google.cloud.pso.bq_security_classifier.helpers.TableScanLimitsConfig;
import com.google.cloud.pso.bq_security_classifier.helpers.TableScanLimitsType;
import com.google.cloud.pso.bq_security_classifier.services.BigQueryService;
import com.google.cloud.pso.bq_security_classifier.services.BigQueryServiceImpl;
import com.google.cloud.pso.bq_security_classifier.services.DlpService;
import com.google.cloud.pso.bq_security_classifier.services.DlpServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.privacy.dlp.v2.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;

import com.google.cloud.pso.bq_security_classifier.helpers.Utils;

public class Inspector implements HttpFunction {

    private final LoggingHelper logger = new LoggingHelper(
            Inspector.class.getSimpleName(),
            applicationName,
            defaultLog,
            trackerLog,
            functionNumber);
    private static final String applicationName = "[bq-security-classifier]";
    private static final String defaultLog = "default-log";
    private static final String trackerLog = "tracker-log";
    private static final Integer functionNumber = 2;
    private static final Gson gson = new Gson();

    private DlpService dlpService;
    private BigQueryService bqService;
    private Environment environment;

    private DlpJob submittedDlpJob;
    public DlpJob getSubmittedDlpJob(){
        return submittedDlpJob;
    }

    private CreateDlpJobRequest createDlpJobRequest;
    public CreateDlpJobRequest  getCreateDlpJobRequest (){
        return createDlpJobRequest;
    }


    public Inspector() throws IOException {
        dlpService = new DlpServiceImpl();
        environment = new Environment();
        bqService = new BigQueryServiceImpl();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response)
            throws IOException {

        var writer = new PrintWriter(response.getWriter());
        String resultMessage = "";

        FunctionOptions options = parseArgs(request);

        logger.logFunctionStart(options.getTrackingId());

        try {
            logger.logInfoWithTracker(options.getTrackingId(),
                    String.format("Parsed arguments %s", options.toString()));

            // get Table Scan Limits config and Table size
            TableScanLimitsConfig tableScanLimitsConfig  = new TableScanLimitsConfig(
                    environment.getTableScanLimitsJsonConfig());

            logger.logInfoWithTracker(options.getTrackingId(),
                    String.format("TableScanLimitsConfig is %s", tableScanLimitsConfig.toString()));

            // DLP job config accepts Integer only for table scan limit. Must downcast
            // NumRows from BigInteger to Integer
            Integer tableNumRows = bqService.getTableNumRows(
                    options.getInputProjectId(),
                    options.getInputDatasetId(),
                    options.getInputTableId()
            ).intValue();

            InspectJobConfig jobConfig = createJob(options.getInputProjectId(),
                    options.getInputDatasetId(),
                    options.getInputTableId(),
                    Integer.parseInt(environment.getSamplingMethod()),
                    tableScanLimitsConfig,
                    tableNumRows,
                    environment.getMinLikelihood(),
                    Integer.parseInt(environment.getMaxFindings()),
                    environment.getProjectId(),
                    environment.getBqResultsDataset(),
                    environment.getBqResultsTable(),
                    environment.getDlpNotificationTopic(),
                    environment.getDlpInspectionTemplateId()
                    );

            createDlpJobRequest = CreateDlpJobRequest.newBuilder()
                            .setJobId(options.getTrackingId())
                            .setParent(LocationName.of(environment.getProjectId(), environment.getRegionId()).toString())
                            .setInspectJob(jobConfig)
                            .build();

            submittedDlpJob = dlpService.submitJob(createDlpJobRequest);

            writer.printf(submittedDlpJob.getName());
            logger.logInfoWithTracker(options.getTrackingId(), String.format("DLP job created successfully id='%s'",
                    submittedDlpJob.getName()));
            logger.logFunctionEnd(options.getTrackingId());
        }catch (Exception ex){

            resultMessage = String.format("Function encountered an exception ='%s'", ex);
            logger.logSevereWithTracker(options.getTrackingId(), resultMessage);
            //to fail the function and report to Cloud Error Reporting.
            throw ex;
        }
    }

    public static InspectJobConfig createJob(
            String targetTableProject,
            String targetTableDataset,
            String targetTable,
            Integer samplingMethod,
            TableScanLimitsConfig rowsLimitConfig,
            Integer tableNumRows,
            String minimumLikelihood,
            Integer maxFindings,
            String resultsProject,
            String resultsDataset,
            String resultsTable,
            String dlpNotificationsTopic,
            String dlpInspectionTemplateId) throws IOException {

        // 1. Specify which table to inspect

        BigQueryTable bqTable = BigQueryTable.newBuilder()
                .setProjectId(targetTableProject)
                .setDatasetId(targetTableDataset)
                .setTableId(targetTable)
                .build();

        BigQueryOptions.Builder bqOptionsBuilder = BigQueryOptions.newBuilder()
                .setTableReference(bqTable)
                .setSampleMethod(BigQueryOptions.SampleMethod.forNumber(samplingMethod));

        Integer limitValue =  rowsLimitConfig.getTableScanLimitBasedOnNumRows(tableNumRows);

        switch (rowsLimitConfig.getScanLimitsType()){
            case NUMBER_OF_ROWS:  bqOptionsBuilder.setRowsLimit(limitValue); break;
            case PERCENTAGE_OF_ROWS: bqOptionsBuilder.setRowsLimitPercent(limitValue); break;
        }

        BigQueryOptions bqOptions = bqOptionsBuilder.build();

        StorageConfig storageConfig =
                StorageConfig.newBuilder()
                        .setBigQueryOptions(bqOptions)
                        .build();

        // The minimum likelihood required before returning a match:
        // See: https://cloud.google.com/dlp/docs/likelihood
        Likelihood minLikelihood = Likelihood.valueOf(minimumLikelihood);

        // The maximum number of findings to report (0 = server maximum)
        InspectConfig.FindingLimits findingLimits =
                InspectConfig.FindingLimits.newBuilder()
                        .setMaxFindingsPerItem(maxFindings)
                        .build();

        InspectConfig inspectConfig =
                InspectConfig.newBuilder()
                        .setIncludeQuote(false) // don't store identified PII in the table
                        .setMinLikelihood(minLikelihood)
                        .setLimits(findingLimits)
                        .build();

        // 2. Specify saving detailed results to BigQuery.

        // Save detailed findings to BigQuery
        BigQueryTable outputBqTable = BigQueryTable.newBuilder()
                .setProjectId(resultsProject)
                .setDatasetId(resultsDataset)
                .setTableId(resultsTable)
                .build();
        OutputStorageConfig outputStorageConfig = OutputStorageConfig.newBuilder()
                .setTable(outputBqTable)
                .build();
        Action.SaveFindings saveFindingsActions = Action.SaveFindings.newBuilder()
                .setOutputConfig(outputStorageConfig)
                .build();
        Action bqAction = Action.newBuilder()
                .setSaveFindings(saveFindingsActions)
                .build();

        // 3. Specify sending PubSub notification on completion.
        Action.PublishToPubSub publishToPubSub = Action.PublishToPubSub.newBuilder()
                .setTopic(dlpNotificationsTopic)
                .build();
        Action pubSubAction = Action.newBuilder()
                .setPubSub(publishToPubSub)
                .build();

        // Configure the inspection job we want the service to perform.
        return InspectJobConfig.newBuilder()
                        .setInspectTemplateName(dlpInspectionTemplateId)
                        .setInspectConfig(inspectConfig)
                        .setStorageConfig(storageConfig)
                        .addActions(bqAction)
                        .addActions(pubSubAction)
                        .build();
    }

    public FunctionOptions parseArgs(HttpRequest request) throws IOException {

        JsonElement requestParsed = gson.fromJson(request.getReader(), JsonElement.class);
        JsonObject requestJson = null;

        if (requestParsed != null && requestParsed.isJsonObject()) {
            requestJson = requestParsed.getAsJsonObject();
        }

        String inputProjectId = Utils.getArgFromJsonOrQueryParams(requestJson, request, "inputProjectId", true);
        String inputDatasetId = Utils.getArgFromJsonOrQueryParams(requestJson, request, "inputDatasetId",true);
        String inputTableId = Utils.getArgFromJsonOrQueryParams(requestJson, request, "inputTableId",true);
        String trackingId = Utils.getArgFromJsonOrQueryParams(requestJson, request, "trackingId",true);

        return new FunctionOptions(
                inputProjectId,
                inputDatasetId,
                inputTableId,
                trackingId
                );
    }
}