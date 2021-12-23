package com.google.cloud.pso.bq_pii_classifier.functions.listener;

public class ListenerConfig {

    private String projectId;
    private String regionId;
    private String taggerTopicId;

    public ListenerConfig(String projectId, String regionId, String taggerTopicId) {
        this.projectId = projectId;
        this.regionId = regionId;
        this.taggerTopicId = taggerTopicId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getTaggerTopicId() {
        return taggerTopicId;
    }

    @Override
    public String toString() {
        return "ListenerConfig{" +
                "projectId='" + projectId + '\'' +
                ", regionId='" + regionId + '\'' +
                ", taggerTopicId='" + taggerTopicId + '\'' +
                '}';
    }
}
