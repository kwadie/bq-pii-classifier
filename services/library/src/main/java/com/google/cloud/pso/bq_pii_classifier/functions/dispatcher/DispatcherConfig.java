package com.google.cloud.pso.bq_pii_classifier.functions.dispatcher;

public class DispatcherConfig {

    private String projectId;
    private String computeRegionId;
    private String dataRegionId;
    private String outputTopic;

    public DispatcherConfig(String projectId, String computeRegionId, String dataRegionId, String outputTopic) {
        this.projectId = projectId;
        this.computeRegionId = computeRegionId;
        this.dataRegionId = dataRegionId;
        this.outputTopic = outputTopic;
    }

    public String getDataRegionId() {
        return dataRegionId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getComputeRegionId() {
        return computeRegionId;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    @Override
    public String toString() {
        return "DispatcherConfig{" +
                "projectId='" + projectId + '\'' +
                ", computeRegionId='" + computeRegionId + '\'' +
                ", dataRegionId='" + dataRegionId + '\'' +
                ", outputTopic='" + outputTopic + '\'' +
                '}';
    }
}
