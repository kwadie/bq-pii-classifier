package com.google.cloud.pso.bq_pii_classifier.services;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.pso.bq_pii_classifier.entities.TableSpec;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public interface BigQueryService {
    String getDatasetLocation(String projectId, String datasetId) throws IOException;

    Job submitJob(String query);

    TableResult waitAndGetJobResults(Job queryJob) throws InterruptedException, RuntimeException;

    List<TableFieldSchema> getTableSchemaFields(TableSpec tableSpec) throws IOException;

    void patchTable(TableSpec tableSpec, List<TableFieldSchema> updatedFields) throws IOException;

    BigInteger getTableNumRows(TableSpec tableSpec) throws IOException;
}
