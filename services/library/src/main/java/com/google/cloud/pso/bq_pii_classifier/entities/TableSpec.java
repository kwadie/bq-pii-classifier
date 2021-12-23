package com.google.cloud.pso.bq_pii_classifier.entities;

import com.google.cloud.pso.bq_pii_classifier.helpers.Utils;

import java.util.List;

public class TableSpec {

    private String project;
    private String dataset;
    private String table;

    public TableSpec(String project, String dataset, String table) {
        this.project = project;
        this.dataset = dataset;
        this.table = table;
    }

    public String getProject() {
        return project;
    }

    public String getDataset() {
        return dataset;
    }

    public String getTable() {
        return table;
    }

    public String toSqlString(){
        return String.format("%s.%s.%s", project, dataset, table);
    }

    // parse from "project.dataset.table" format
    public static TableSpec fromSqlString(String sqlTableId){
        List<String> targetTableSpecs = Utils.tokenize(sqlTableId, ".", true);
        return new TableSpec(
                targetTableSpecs.get(0),
                targetTableSpecs.get(1),
                targetTableSpecs.get(2)
        );
    }
}
