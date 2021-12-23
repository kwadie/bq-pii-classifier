package com.google.cloud.pso.bq_pii_classifier.functions.inspector;


import com.google.cloud.pso.bq_pii_classifier.entities.TableOperationRequest;
import com.google.cloud.pso.bq_pii_classifier.entities.TableScanLimitsConfig;
import com.google.cloud.pso.bq_pii_classifier.entities.TableSpec;
import com.google.cloud.pso.bq_pii_classifier.functions.tagger.Tagger;
import com.google.cloud.pso.bq_pii_classifier.helpers.LoggingHelper;
import com.google.cloud.pso.bq_pii_classifier.helpers.Utils;
import com.google.cloud.pso.bq_pii_classifier.services.BigQueryService;
import com.google.cloud.pso.bq_pii_classifier.services.DlpService;
import com.google.privacy.dlp.v2.*;

import java.io.IOException;
import java.util.List;

public class Inspector {

    private final LoggingHelper logger;

    private static final Integer functionNumber = 2;

    private InspectorConfig config;
    private DlpService dlpService;
    private BigQueryService bqService;


    public Inspector(InspectorConfig config, DlpService dlpService, BigQueryService bqService){
        this.config = config;
        this.dlpService = dlpService;
        this.bqService = bqService;

        logger = new LoggingHelper(
                Inspector.class.getSimpleName(),
                functionNumber,
                config.getProjectId()
        );
    }

    public DlpJob execute(TableOperationRequest request, String trackingId) throws IOException {

        logger.logFunctionStart(trackingId);

        logger.logInfoWithTracker(trackingId, String.format("Request : %s", request.toString()));

        // get Table Scan Limits config and Table size
        TableScanLimitsConfig tableScanLimitsConfig  = new TableScanLimitsConfig(
                config.getTableScanLimitsJsonConfig());

        logger.logInfoWithTracker(trackingId,
                String.format("TableScanLimitsConfig is %s", tableScanLimitsConfig.toString()));

        // DLP job config accepts Integer only for table scan limit. Must downcast
        // NumRows from BigInteger to Integer
        TableSpec targetTableSpec = TableSpec.fromSqlString(request.getTableSpec());

        Integer tableNumRows = bqService.getTableNumRows(targetTableSpec).intValue();

        InspectJobConfig jobConfig = createJob(
                targetTableSpec,
                tableScanLimitsConfig,
                tableNumRows,
                config
        );

        CreateDlpJobRequest createDlpJobRequest = CreateDlpJobRequest.newBuilder()
                .setJobId(trackingId) // Letters, numbers, hyphens, and underscores allowed.
                .setParent(LocationName.of(config.getProjectId(), config.getRegionId()).toString())
                .setInspectJob(jobConfig)
                .build();

        DlpJob submittedDlpJob = dlpService.submitJob(createDlpJobRequest);

        logger.logInfoWithTracker(trackingId, String.format("DLP job created successfully id='%s'",
                submittedDlpJob.getName()));

        logger.logFunctionEnd(trackingId);

        return submittedDlpJob;
    }

    private InspectJobConfig createJob(
            TableSpec targetTableSpec,
            TableScanLimitsConfig rowsLimitConfig,
            Integer tableNumRows,
            InspectorConfig config){

        // 1. Specify which table to inspect

        BigQueryTable bqTable = BigQueryTable.newBuilder()
                .setProjectId(targetTableSpec.getProject())
                .setDatasetId(targetTableSpec.getDataset())
                .setTableId(targetTableSpec.getTable())
                .build();

        BigQueryOptions.Builder bqOptionsBuilder = BigQueryOptions.newBuilder()
                .setTableReference(bqTable)
                .setSampleMethod(BigQueryOptions.SampleMethod.forNumber(config.getSamplingMethod()));

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
        Likelihood minLikelihood = Likelihood.valueOf(config.getMinLikelihood());

        // The maximum number of findings to report (0 = server maximum)
        InspectConfig.FindingLimits findingLimits =
                InspectConfig.FindingLimits.newBuilder()
                        .setMaxFindingsPerItem(config.getMaxFindings())
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
                .setProjectId(config.getProjectId())
                .setDatasetId(config.getBqResultsDataset())
                .setTableId(config.getBqResultsTable())
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
                .setTopic(config.getDlpNotificationTopic())
                .build();
        Action pubSubAction = Action.newBuilder()
                .setPubSub(publishToPubSub)
                .build();

        // Configure the inspection job we want the service to perform.
        return InspectJobConfig.newBuilder()
                .setInspectTemplateName(config.getDlpInspectionTemplateId())
                .setInspectConfig(inspectConfig)
                .setStorageConfig(storageConfig)
                .addActions(bqAction)
                .addActions(pubSubAction)
                .build();
    }


}
