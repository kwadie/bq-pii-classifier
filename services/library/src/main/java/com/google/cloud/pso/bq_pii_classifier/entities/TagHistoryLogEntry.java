package com.google.cloud.pso.bq_pii_classifier.entities;

import com.google.cloud.pso.bq_pii_classifier.functions.tagger.ColumnTaggingAction;
import org.slf4j.event.Level;

public class TagHistoryLogEntry {

    private TableSpec tableSpec;
    private String fieldName;
    private String existingPolicyTagId;
    private String newPolicyTagId;
    private ColumnTaggingAction columnTaggingAction;
    private String description;
    private Level logLevel;

    public TagHistoryLogEntry(TableSpec tableSpec, String fieldName, String existingPolicyTagId, String newPolicyTagId, ColumnTaggingAction columnTaggingAction, String description, Level logLevel) {
        this.tableSpec = tableSpec;
        this.fieldName = fieldName;
        this.existingPolicyTagId = existingPolicyTagId;
        this.newPolicyTagId = newPolicyTagId;
        this.columnTaggingAction = columnTaggingAction;
        this.description = description;
        this.logLevel = logLevel;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getExistingPolicyTagId() {
        return existingPolicyTagId;
    }

    public String getNewPolicyTagId() {
        return newPolicyTagId;
    }

    public ColumnTaggingAction getColumnTaggingAction() {
        return columnTaggingAction;
    }

    public String getDescription() {
        return description;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public TableSpec getTableSpec() {
        return tableSpec;
    }

    public String toLogString() {

        return String.format("%s | %s | %s | %s | %s | %s | %s | %s",
                tableSpec.getProject(),
                tableSpec.getDataset(),
                tableSpec.getTable(),
                fieldName,
                existingPolicyTagId,
                newPolicyTagId,
                columnTaggingAction,
                description
        );
    }
}
