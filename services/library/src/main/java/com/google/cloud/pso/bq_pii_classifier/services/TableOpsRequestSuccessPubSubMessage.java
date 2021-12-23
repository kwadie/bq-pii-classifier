package com.google.cloud.pso.bq_pii_classifier.services;

import com.google.cloud.pso.bq_pii_classifier.entities.TableOperationRequest;

public class TableOpsRequestSuccessPubSubMessage {

    private TableOperationRequest msg;
    private String msgId;

    public TableOpsRequestSuccessPubSubMessage(TableOperationRequest msg, String msgId) {
        this.msg = msg;
        this.msgId = msgId;
    }

    public TableOperationRequest getMsg() {
        return msg;
    }

    public String getMsgId() {
        return msgId;
    }

    @Override
    public String toString() {
        return "PubSubSuccessMessage{" +
                "msg='" + msg + '\'' +
                ", msgId='" + msgId + '\'' +
                '}';
    }
}
