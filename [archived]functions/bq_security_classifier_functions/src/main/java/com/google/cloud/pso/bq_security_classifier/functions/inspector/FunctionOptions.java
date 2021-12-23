package com.google.cloud.pso.bq_security_classifier.functions.inspector;

public class FunctionOptions {
    private String inputProjectId;
    private String inputDatasetId;
    private String inputTableId;
    // Letters, numbers, hyphens, and underscores allowed.
    private String trackingId;

    public FunctionOptions(String inputProjectId, String inputDatasetId, String inputTableId, String trackingId) {
        this.inputProjectId = inputProjectId;
        this.inputDatasetId = inputDatasetId;
        this.inputTableId = inputTableId;
        this.trackingId = trackingId;
    }

    public String getInputProjectId() {
        return inputProjectId;
    }

    public void setInputProjectId(String inputProjectId) {
        this.inputProjectId = inputProjectId;
    }

    public String getInputDatasetId() {
        return inputDatasetId;
    }

    public void setInputDatasetId(String inputDatasetId) {
        this.inputDatasetId = inputDatasetId;
    }

    public String getInputTableId() {
        return inputTableId;
    }

    public void setInputTableId(String inputTableId) {
        this.inputTableId = inputTableId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    @Override
    public String toString() {
        return "FunctionOptions{" +
                "inputProjectId='" + inputProjectId + '\'' +
                ", inputDatasetId='" + inputDatasetId + '\'' +
                ", inputTableId='" + inputTableId + '\'' +
                ", trackingId='" + trackingId + '\'' +
                '}';
    }
}
