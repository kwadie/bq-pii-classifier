package com.google.cloud.pso.bq_pii_classifier.functions.listener;

import com.google.cloud.pso.bq_pii_classifier.entities.NonRetryableApplicationException;
import com.google.cloud.pso.bq_pii_classifier.entities.TableOperationRequest;
import com.google.cloud.pso.bq_pii_classifier.entities.TableSpec;
import com.google.cloud.pso.bq_pii_classifier.helpers.LoggingHelper;
import com.google.cloud.pso.bq_pii_classifier.helpers.TrackingHelper;
import com.google.cloud.pso.bq_pii_classifier.services.DlpService;
import com.google.cloud.pso.bq_pii_classifier.services.PubSubPublishResults;
import com.google.cloud.pso.bq_pii_classifier.services.PubSubService;
import com.google.cloud.pso.bq_pii_classifier.services.TableOpsRequestFailedPubSubMessage;
import com.google.cloud.pso.bq_pii_classifier.services.TableOpsRequestSuccessPubSubMessage;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.DlpJob;

import java.io.IOException;
import java.util.Arrays;

public class Listener {

    private static final Integer functionNumber = 3;

    private final LoggingHelper logger;

    private ListenerConfig config;
    private DlpService dlpService;
    private PubSubService pubSubService;


    public Listener(ListenerConfig config, DlpService dlpService, PubSubService pubSubService){
        this.config = config;
        this.dlpService = dlpService;
        this.pubSubService = pubSubService;

        logger = new LoggingHelper(
                Listener.class.getSimpleName(),
                functionNumber,
                config.getProjectId()
        );
    }

    public PubSubPublishResults execute(String dlpJobName, String trackingId) throws NonRetryableApplicationException, IOException, InterruptedException {

        logger.logFunctionStart(trackingId);

        logger.logInfoWithTracker(trackingId, String.format("Received DlpJobName %s", dlpJobName));

        DlpJob.JobState dlpJobState = dlpService.getJobState(dlpJobName);

        if (dlpJobState != DlpJob.JobState.DONE) {
            String msg = String.format("DLP Job '%s' state must be 'DONE'. Current state : '%s'. Function call will terminate. ",
                    dlpJobName,
                    dlpJobState);
            logger.logSevereWithTracker(trackingId, msg);
            // this shouldn't happen because DLP shouldn't send the message before teh job finishes. That's why it's NonRetryable
            throw new NonRetryableApplicationException(msg);
        }

        BigQueryTable inspectedTable = dlpService.getInspectedTable(dlpJobName);
        String inspectedTableSpec = new TableSpec(
                inspectedTable.getProjectId(),
                inspectedTable.getDatasetId(),
                inspectedTable.getTableId()
        ).toSqlString();

        TableOperationRequest taggerRequest = new TableOperationRequest(
                inspectedTableSpec,
                TrackingHelper.parseRunIdAsPrefix(trackingId),
                trackingId
        );

        PubSubPublishResults pubSubPublishResults = pubSubService.publishTableOperationRequests(
                config.getProjectId(),
                config.getTaggerTopicId(),
                Arrays.asList(taggerRequest)
        );

        for(TableOpsRequestFailedPubSubMessage msg: pubSubPublishResults.getFailedMessages()){
            String logMsg = String.format("Failed to publish this messages %s", msg.toString());
            logger.logWarnWithTracker(trackingId, logMsg);
        }

        for(TableOpsRequestSuccessPubSubMessage msg: pubSubPublishResults.getSuccessMessages()){
            String logMsg = String.format("Successfully publish this messages %s", msg.toString());
            logger.logInfoWithTracker(trackingId, logMsg);
        }

        logger.logFunctionEnd(trackingId);

        return pubSubPublishResults;
    }


}
