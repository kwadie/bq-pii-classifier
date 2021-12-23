package com.google.cloud.pso.bq_pii_classifier.entities;

import com.google.gson.Gson;

public class TableOperationRequest {

    private String tableSpec;
    private String runId;
    private String trackingId;

    public TableOperationRequest() {
    }

    public TableOperationRequest(String tableSpec, String runId, String trackingId) {
        this.tableSpec = tableSpec;
        this.runId = runId;
        this.trackingId = trackingId;
    }

    public String getTableSpec() {
        return tableSpec;
    }

    public String getRunId() {
        return runId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTableSpec(String tableSpec) {
        this.tableSpec = tableSpec;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    @Override
    public String toString() {
        return "TaggerRequest{" +
                "tableSpec='" + tableSpec + '\'' +
                ", runId='" + runId + '\'' +
                ", trackerId='" + trackingId + '\'' +
                '}';
    }

    public String toJsonString (){
        return new Gson().toJson(this);

    }
}
