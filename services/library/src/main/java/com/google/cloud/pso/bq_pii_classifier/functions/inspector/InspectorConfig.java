package com.google.cloud.pso.bq_pii_classifier.functions.inspector;

public class InspectorConfig {


    private String projectId;
    private String regionId;
    private String bqResultsDataset;
    private String bqResultsTable;
    private String dlpNotificationTopic;
    private String minLikelihood;
    private Integer maxFindings;
    private Integer samplingMethod;
    private String dlpInspectionTemplateId;
    private String tableScanLimitsJsonConfig;

    public InspectorConfig(String projectId, String regionId, String bqResultsDataset, String bqResultsTable, String dlpNotificationTopic, String minLikelihood, Integer maxFindings, Integer samplingMethod, String dlpInspectionTemplateId, String tableScanLimitsJsonConfig) {
        this.projectId = projectId;
        this.regionId = regionId;
        this.bqResultsDataset = bqResultsDataset;
        this.bqResultsTable = bqResultsTable;
        this.dlpNotificationTopic = dlpNotificationTopic;
        this.minLikelihood = minLikelihood;
        this.maxFindings = maxFindings;
        this.samplingMethod = samplingMethod;
        this.dlpInspectionTemplateId = dlpInspectionTemplateId;
        this.tableScanLimitsJsonConfig = tableScanLimitsJsonConfig;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getBqResultsDataset() {
        return bqResultsDataset;
    }

    public String getBqResultsTable() {
        return bqResultsTable;
    }

    public String getDlpNotificationTopic() {
        return dlpNotificationTopic;
    }

    public String getMinLikelihood() {
        return minLikelihood;
    }

    public Integer getMaxFindings() {
        return maxFindings;
    }

    public Integer getSamplingMethod() {
        return samplingMethod;
    }

    public String getDlpInspectionTemplateId() {
        return dlpInspectionTemplateId;
    }

    public String getTableScanLimitsJsonConfig() {
        return tableScanLimitsJsonConfig;
    }

    @Override
    public String toString() {
        return "InspectorConfig{" +
                "projectId='" + projectId + '\'' +
                ", regionId='" + regionId + '\'' +
                ", bqResultsDataset='" + bqResultsDataset + '\'' +
                ", bqResultsTable='" + bqResultsTable + '\'' +
                ", dlpNotificationTopic='" + dlpNotificationTopic + '\'' +
                ", minLikelihood='" + minLikelihood + '\'' +
                ", maxFindings='" + maxFindings + '\'' +
                ", samplingMethod='" + samplingMethod + '\'' +
                ", dlpInspectionTemplateId='" + dlpInspectionTemplateId + '\'' +
                ", tableScanLimitsJsonConfig='" + tableScanLimitsJsonConfig + '\'' +
                '}';
    }
}
