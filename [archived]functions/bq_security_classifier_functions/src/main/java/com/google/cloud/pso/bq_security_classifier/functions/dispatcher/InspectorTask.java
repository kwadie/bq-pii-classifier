package com.google.cloud.pso.bq_security_classifier.functions.dispatcher;

public class InspectorTask {

    private String targetTableProject;
    private String targetTableDataset;
    private String targetTable;
    private String queueId;
    private String httpEndPoint;
    private String trackingId;


    public String getTargetTableProject() {
        return targetTableProject;
    }

    public void setTargetTableProject(String targetTableProject) {
        this.targetTableProject = targetTableProject;
    }

    public String getTargetTableDataset() {
        return targetTableDataset;
    }

    public void setTargetTableDataset(String targetTableDataset) {
        this.targetTableDataset = targetTableDataset;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }


    public String getHttpEndPoint() {
        return httpEndPoint;
    }

    public void setHttpEndPoint(String httpEndPoint) {
        this.httpEndPoint = httpEndPoint;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    @Override
    public String toString() {
        return "functions.InspectorTask{" +
                "targetTableProject='" + targetTableProject + '\'' +
                ", targetTableDataset='" + targetTableDataset + '\'' +
                ", targetTable='" + targetTable + '\'' +
                ", queuePath='" + queueId + '\'' +
                ", httpEndPoint='" + httpEndPoint + '\'' +
                ", trackingId='" + trackingId + '\'' +
                '}';
    }
}
