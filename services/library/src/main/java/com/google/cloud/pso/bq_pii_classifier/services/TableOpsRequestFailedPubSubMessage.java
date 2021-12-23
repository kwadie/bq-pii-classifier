package com.google.cloud.pso.bq_pii_classifier.services;

import com.google.cloud.pso.bq_pii_classifier.entities.TableOperationRequest;

public class TableOpsRequestFailedPubSubMessage {

    private TableOperationRequest msg;
    private Exception exception;


    public TableOpsRequestFailedPubSubMessage(TableOperationRequest msg, Exception exception) {
        this.msg = msg;
        this.exception = exception;
    }

    public TableOperationRequest getMsg() {
        return msg;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "PubSubFailedMessage{" +
                "msg='" + msg + '\'' +
                ", exception=" + exception +
                '}';
    }
}
