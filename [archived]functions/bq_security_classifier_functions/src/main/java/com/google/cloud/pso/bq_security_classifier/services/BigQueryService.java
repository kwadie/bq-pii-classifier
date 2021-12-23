package com.google.cloud.pso.bq_security_classifier.services;

import com.google.api.gax.paging.Page;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.cloud.bigquery.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Stream;

public interface BigQueryService {
    String getDatasetLocation(String projectId, String datasetId) throws IOException;

    Stream<TableId> listTables(String projectId, String datasetId);

    Stream<DatasetId> listDatasets(String projectId);

    Job submitJob(QueryJobConfiguration queryJobConfiguration);

    TableResult waitAndGetJobResults(Job queryJob) throws InterruptedException, RuntimeException;

    List<TableFieldSchema> getTableSchemaFields(String projectId, String datasetId, String tableId) throws IOException;

    void patchTable(String projectId, String datasetId, String tableId, List<TableFieldSchema> updatedFields) throws IOException;

    BigInteger getTableNumRows(String projectId, String datasetId, String tableId) throws IOException;
}
