package com.google.cloud.pso.bq_security_classifier.services;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BigQueryServiceImpl implements BigQueryService {

    private com.google.api.services.bigquery.Bigquery bqAPI;
    private BigQuery bqAPIWrapper;

    public BigQueryServiceImpl() throws IOException {
        bqAPIWrapper = BigQueryOptions.getDefaultInstance().getService();

        // direct API calls are needed for some operations
        // TODO: follow up on the missing/faulty wrapper calls and stop using direct API calls
        bqAPI = new com.google.api.services.bigquery.Bigquery.Builder(
                new NetHttpTransport(),
                new JacksonFactory(),
                new HttpCredentialsAdapter(GoogleCredentials
                        .getApplicationDefault()
                        .createScoped(BigqueryScopes.all())))
                .setApplicationName("bq-security-classifier")
                .build();
    }

    @Override
    public String getDatasetLocation(String projectId, String datasetId) throws IOException {
        // calling dataset.getLocation always returns null --> seems like a bug in the SDK
        // instead, use the underlying API call to get dataset info
        return bqAPI.datasets()
                .get(projectId, datasetId)
                .execute()
                .getLocation();
    }

    @Override
    public Stream<TableId> listTables(String projectId, String datasetId){
        return StreamSupport.stream(
                bqAPIWrapper.listTables(DatasetId.of(projectId, datasetId)).iterateAll().spliterator(),
                false).map(Table::getTableId);
    }

    @Override
    public Stream<DatasetId> listDatasets(String projectId){
        return StreamSupport.stream(
                bqAPIWrapper.listDatasets(projectId).iterateAll().spliterator(),
                false).map(Dataset::getDatasetId);
    }

    @Override
    public Job submitJob(QueryJobConfiguration queryJobConfiguration){
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        return bqAPIWrapper.create(JobInfo.newBuilder(queryJobConfiguration).setJobId(jobId).build());
    }

    @Override
    public TableResult waitAndGetJobResults(Job queryJob) throws InterruptedException, RuntimeException {
        // Wait for the query to complete.
        queryJob = queryJob.waitFor();

        // Check for errors
        if (queryJob == null) {
            throw new RuntimeException("Job no longer exists");
        } else // You can also look at queryJob.getStatus().getExecutionErrors() for all
            // errors, not just the latest one.
            if (queryJob.getStatus().getError() != null) {
                throw new RuntimeException(queryJob.getStatus().getError().toString());
            }

        return queryJob.getQueryResults();
    }

    @Override
    public List<TableFieldSchema> getTableSchemaFields(String projectId, String datasetId, String tableId) throws IOException {

        return bqAPI.tables()
                .get(projectId, datasetId, tableId)
                .execute()
                .getSchema()
                .getFields();
    }

    @Override
    public void patchTable(String projectId, String datasetId, String tableId, List<TableFieldSchema> updatedFields) throws IOException {
        bqAPI.tables()
                .patch(projectId,
                        datasetId,
                        tableId,
                        new com.google.api.services.bigquery.model.Table().setSchema(new TableSchema().setFields(updatedFields)))
                .execute();
    }

    @Override
    public BigInteger getTableNumRows(String projectId, String datasetId, String tableId) throws IOException {
        return bqAPI.tables()
                .get(projectId, datasetId, tableId)
                .execute()
                .getNumRows();
    }


}
