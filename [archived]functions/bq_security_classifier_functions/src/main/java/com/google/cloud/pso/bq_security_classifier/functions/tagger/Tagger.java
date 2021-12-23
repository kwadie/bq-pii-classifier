package com.google.cloud.pso.bq_security_classifier.functions.tagger;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.bigquery.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Tuple;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;

import com.google.api.services.bigquery.model.TableFieldSchema.PolicyTags;
import com.google.api.services.bigquery.model.Table;
import com.google.cloud.pso.bq_security_classifier.services.BigQueryService;
import com.google.cloud.pso.bq_security_classifier.services.BigQueryServiceImpl;
import com.google.cloud.pso.bq_security_classifier.services.DlpService;
import com.google.cloud.pso.bq_security_classifier.services.DlpServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.cloud.pso.bq_security_classifier.helpers.LoggingHelper;
import com.google.cloud.pso.bq_security_classifier.helpers.Utils;

public class Tagger implements HttpFunction {

    private final LoggingHelper logger = new LoggingHelper(
            Tagger.class.getSimpleName(),
            applicationName,
            defaultLog,
            trackerLog,
            functionNumber);
    private static final String applicationName = "[bq-security-classifier]";
    private static final String defaultLog = "default-log";
    private static final String tagHistoryLog = "tag-history-log";
    private static final String trackerLog = "tracker-log";
    private static final Integer functionNumber = 4;

    private static final Gson gson = new Gson();

    TaggerHelper taggerHelper;
    DlpService dlpService;
    BigQueryService bqService;
    Environment environment;

    // output of the call
    Map<String, String> finalFieldsToPolicyTags;

    public Map<String, String> getFinalFieldsToPolicyTags() {
        return finalFieldsToPolicyTags;
    }


    public Tagger() throws IOException {
        dlpService = new DlpServiceImpl();
        bqService = new BigQueryServiceImpl();
        environment = new Environment();
        finalFieldsToPolicyTags = new HashMap<>();
        taggerHelper = new TaggerHelper(bqService);
    }


    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException, InterruptedException {

        var writer = new PrintWriter(response.getWriter());
        FunctionOptions options = parseArgs(request);

        // dlp job is created using the trackingId via the Inspector CF
        String trackingId = Utils.extractTrackingIdFromJobName(options.getDlpJobName());

        logger.logFunctionStart(trackingId);

        logger.logInfoWithTracker(trackingId, String.format("Parsed arguments : %s", options.toString()));

        try {

            DlpJob.JobState dlpJobState = dlpService.getJobState(options.getDlpJobName());

            if (dlpJobState != DlpJob.JobState.DONE) {
                String msg = String.format("DLP Job '%s' state must be 'DONE'. Current state : '%s'. Function call will terminate. ",
                        options.getDlpJobName(),
                        dlpJobState);
                logger.logSevereWithTracker(trackingId, msg);
                writer.printf(msg);
                throw new RuntimeException(msg);
            }

            BigQueryTable inspectedTable = dlpService.getInspectedTable(options.getDlpJobName());

            // Query DLP results in BQ and return a dict of bq_column=policy_tag
            Map<String, String> fieldsToPolicyTagsMap = taggerHelper.getFieldsToPolicyTagsMap(
                    environment.getProjectId(),
                    environment.getDatasetId(),
                    environment.getBqViewFieldsFindings(),
                    options.getDlpJobName());

            // If these are PII detected
            if (!fieldsToPolicyTagsMap.isEmpty()){

                // check if any policy tag id is empty
                if(fieldsToPolicyTagsMap.values().stream().anyMatch(String::isBlank)){
                    String msg = String.format("Computed Fields to Policy Tags mapping contains empty policy tag ids. Computed mapping is %s", fieldsToPolicyTagsMap.toString());
                    throw new RuntimeException(msg);
                }

                logger.logInfoWithTracker(trackingId, String.format("Computed Fields to Policy Tags mapping : %s", fieldsToPolicyTagsMap.toString()));

                // Apply policy tags to columns in BigQuery
                // If isDryRun = True no actual tagging will happen on BogQuery and Dry-Run log entries will be written instead
                List<TableFieldSchema> updatedFields = applyPolicyTags(
                        inspectedTable.getProjectId(),
                        inspectedTable.getDatasetId(),
                        inspectedTable.getTableId(),
                        fieldsToPolicyTagsMap,
                        new HashSet(Utils.tokenize(environment.getTaxonomies(), ",", true)),
                        environment.getIsDryRun(),
                        trackingId);

                // used in unit testing
                finalFieldsToPolicyTags = mapFieldsToPolicyTags(updatedFields);

            }else{
                // If no PII detected
                logger.logInfoWithTracker(trackingId, "No DLP results detected.");

            }

            logger.logFunctionEnd(trackingId);

            writer.printf("Call completed successfully.");

        } catch (Exception ex) {
            logger.logSevereWithTracker(trackingId, String.format("Exception: %s", ex.toString()));
            writer.printf("Call Failed. Check logs for more details. %s", ex.getMessage());
            //rethrow to appear in Cloud Error Reporting
            throw ex;
        }
    }


    public FunctionOptions parseArgs(HttpRequest request) throws IOException {

        try {
            JsonElement requestParsed = gson.fromJson(request.getReader(), JsonElement.class);
            JsonObject requestJson = null;

            if (requestParsed != null && requestParsed.isJsonObject()) {
                requestJson = requestParsed.getAsJsonObject();
            }

            String dlpJobName = Utils.getArgFromJsonOrQueryParams(requestJson, request, "dlpJobName", true);

            return new FunctionOptions(dlpJobName);

        } catch (JsonParseException e) {

            logger.logSevereWithTracker("", "Error parsing JSON: " + e.getMessage());
            throw e;
        }
    }

    public List<TableFieldSchema> applyPolicyTags(String projectId,
                                                  String datasetId,
                                                  String tableId,
                                                  Map<String, String> fieldsToPolicyTagsMap,
                                                  Set<String> app_managed_taxonomies,
                                                  Boolean isDryRun,
                                                  String trackingId) throws IOException {

        List<TableFieldSchema> currentFields = bqService.getTableSchemaFields(projectId, datasetId, tableId);
        List<TableFieldSchema> updatedFields = new ArrayList<>();

        // store all actions on policy tags and log them after patching the BQ table
        List<Tuple<String, Level>> policyUpdateLogs = new ArrayList<>();

        for (TableFieldSchema field : currentFields) {
            if (fieldsToPolicyTagsMap.containsKey(field.getName())) {

                String newPolicyTagId = fieldsToPolicyTagsMap.get(field.getName()).trim();

                PolicyTags fieldPolicyTags = field.getPolicyTags();

                // if no policy exists on the field, attach one
                if (fieldPolicyTags == null) {

                    // update the field with policy tag
                    fieldPolicyTags = new PolicyTags().setNames(Arrays.asList(newPolicyTagId));
                    field.setPolicyTags(fieldPolicyTags);

                    String logMsg = String.format("%s | %s | %s | %s | %s | %s | %s | %s",
                            projectId,
                            datasetId,
                            tableId,
                            field.getName(),
                            "",
                            newPolicyTagId,
                            isDryRun? ColumnTaggingAction.DRY_RUN_CREATE: ColumnTaggingAction.CREATE,
                            ""
                    );
                    policyUpdateLogs.add(Tuple.of(logMsg, Level.INFO));
                } else {
                    String existingPolicyTagId = fieldPolicyTags.getNames().get(0).trim();

                    // overwrite policy tag if it belongs to the same taxonomy only
                    String existingTaxonomy = Utils.extractTaxonomyIdFromPolicyTagId(existingPolicyTagId);
                    String newTaxonomy = Utils.extractTaxonomyIdFromPolicyTagId(newPolicyTagId);

                    // update existing tags only if they belong to the security classifier application.
                    // Don't overwrite manually created taxonomies
                    if (app_managed_taxonomies.contains(existingTaxonomy)) {

                        if(existingPolicyTagId.equals(newPolicyTagId)){

                            // policy tag didn't change
                            String logMsg = String.format("%s | %s | %s | %s | %s | %s | %s | %s",
                                    projectId,
                                    datasetId,
                                    tableId,
                                    field.getName(),
                                    existingPolicyTagId,
                                    newPolicyTagId,
                                    isDryRun? ColumnTaggingAction.DRY_RUN_NO_CHANGE: ColumnTaggingAction.NO_CHANGE,
                                    "Existing policy tag is the same as newly computed tag."
                            );
                            policyUpdateLogs.add(Tuple.of(logMsg, Level.INFO));

                        }else{
                            // update the field with a new policy tag
                            fieldPolicyTags.setNames(Arrays.asList(newPolicyTagId));

                            String logMsg = String.format("%s | %s | %s | %s | %s | %s | %s | %s",
                                    projectId,
                                    datasetId,
                                    tableId,
                                    field.getName(),
                                    existingPolicyTagId,
                                    newPolicyTagId,
                                    isDryRun? ColumnTaggingAction.DRY_RUN_OVERWRITE: ColumnTaggingAction.OVERWRITE,
                                    ""
                            );
                            policyUpdateLogs.add(Tuple.of(logMsg, Level.INFO));
                        }
                    } else {

                        // if new taxonomy doesn't belong to the BQ security classifier app (e.g. manually created)
                        String logMsg = String.format("%s | %s | %s | %s | %s | %s | %s | %s",
                                projectId,
                                datasetId,
                                tableId,
                                field.getName(),
                                existingPolicyTagId,
                                newPolicyTagId,
                                isDryRun? ColumnTaggingAction.DRY_RUN_KEEP_EXISTING: ColumnTaggingAction.KEEP_EXISTING,
                                "Can't overwrite tags that are not crated/managed by the application. The existing taxonomy is created by another app/user"
                        );
                        policyUpdateLogs.add(Tuple.of(logMsg, Level.WARNING));
                    }
                }
            }
            // add all fields that exists in the table (after updates) to be able to patch the table
            updatedFields.add(field);
        }

        // if it's not a dry run, patch the table with the new schema including new policy tags
        if (!isDryRun) {
            bqService.patchTable(projectId, datasetId, tableId, updatedFields);
        }


        // log all actions on policy tags after bq.tables.patch operation is successful
        for (Tuple<String, Level> t : policyUpdateLogs) {
            logger.logWithTracker(tagHistoryLog, trackingId, t.x(), t.y());
        }

        return updatedFields;
    }



    public static Map<String, String> mapFieldsToPolicyTags(List<TableFieldSchema> fields){
        Map<String, String> result = new HashMap<>();
        for(TableFieldSchema field: fields){
            PolicyTags policyTags = field.getPolicyTags();
            if(policyTags == null){
                result.put(field.getName(),"");
            }else{
                // only one policy tag per column is allowed by BigQuery
                result.put(field.getName(),policyTags.getNames().get(0));
            }
        }
        return result;
    }

}