package com.google.cloud.pso.bq_pii_classifier.functions.dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BigQueryScope {

    private List<String> projectIncludeList;
    private List<String> datasetIncludeList;
    private List<String> datasetExcludeList;
    private List<String> tableIncludeList;
    private List<String> tableExcludeList;


    public BigQueryScope() {
        this.projectIncludeList = new ArrayList<>();
        this.datasetIncludeList = new ArrayList<>();
        this.datasetExcludeList = new ArrayList<>();
        this.tableIncludeList = new ArrayList<>();
        this.tableExcludeList = new ArrayList<>();
    }

    public BigQueryScope(List<String> projectIncludeList, List<String> datasetIncludeList, List<String> datasetExcludeList, List<String> tableIncludeList, List<String> tableExcludeList) {
        this.projectIncludeList = projectIncludeList;
        this.datasetIncludeList = datasetIncludeList;
        this.datasetExcludeList = datasetExcludeList;
        this.tableIncludeList = tableIncludeList;
        this.tableExcludeList = tableExcludeList;
    }

    public List<String> getProjectIncludeList() {
        return projectIncludeList;
    }

    public List<String> getDatasetIncludeList() {
        return datasetIncludeList;
    }

    public List<String> getDatasetExcludeList() {
        return datasetExcludeList;
    }

    public List<String> getTableIncludeList() {
        return tableIncludeList;
    }

    public List<String> getTableExcludeList() {
        return tableExcludeList;
    }

    public void setProjectIncludeList(List<String> projectIncludeList) {
        this.projectIncludeList = projectIncludeList;
    }

    public void setDatasetIncludeList(List<String> datasetIncludeList) {
        this.datasetIncludeList = datasetIncludeList;
    }

    public void setDatasetExcludeList(List<String> datasetExcludeList) {
        this.datasetExcludeList = datasetExcludeList;
    }

    public void setTableIncludeList(List<String> tableIncludeList) {
        this.tableIncludeList = tableIncludeList;
    }

    public void setTableExcludeList(List<String> tableExcludeList) {
        this.tableExcludeList = tableExcludeList;
    }

    @Override
    public String toString() {
        return "BigQueryScope{" +
                "projectIncludeList=" + projectIncludeList +
                ", datasetIncludeList=" + datasetIncludeList +
                ", datasetExcludeList=" + datasetExcludeList +
                ", tableIncludeList=" + tableIncludeList +
                ", tableExcludeList=" + tableExcludeList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BigQueryScope that = (BigQueryScope) o;
        return Objects.equals(projectIncludeList, that.projectIncludeList) &&
                Objects.equals(datasetIncludeList, that.datasetIncludeList) &&
                Objects.equals(datasetExcludeList, that.datasetExcludeList) &&
                Objects.equals(tableIncludeList, that.tableIncludeList) &&
                Objects.equals(tableExcludeList, that.tableExcludeList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectIncludeList, datasetIncludeList, datasetExcludeList, tableIncludeList, tableExcludeList);
    }
}
