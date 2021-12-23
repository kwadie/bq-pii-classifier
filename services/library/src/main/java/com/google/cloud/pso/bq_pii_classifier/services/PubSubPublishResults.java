package com.google.cloud.pso.bq_pii_classifier.services;

import java.util.List;

public class PubSubPublishResults {

    private List<TableOpsRequestSuccessPubSubMessage> successMessages;
    private List<TableOpsRequestFailedPubSubMessage> failedMessages;

    public PubSubPublishResults(List<TableOpsRequestSuccessPubSubMessage> successMessages, List<TableOpsRequestFailedPubSubMessage> failedMessages) {
        this.successMessages = successMessages;
        this.failedMessages = failedMessages;
    }

    public List<TableOpsRequestSuccessPubSubMessage> getSuccessMessages() {
        return successMessages;
    }

    public List<TableOpsRequestFailedPubSubMessage> getFailedMessages() {
        return failedMessages;
    }

    @Override
    public String toString() {
        return "PubSubPublishResults{" +
                "successMessages=" + successMessages +
                ", failedMessages=" + failedMessages +
                '}';
    }
}
