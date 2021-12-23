package com.google.cloud.pso.bq_security_classifier.services;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.cloud.bigquery.*;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

//  Using Mockito mocks instead
// TODO: delete file if not needed
//
//public class BigQueryServiceTestImpl implements BigQueryService {
//
//    List<TableId> tableCatalog = Lists.newArrayList (
//            TableId.of("p1", "d1", "t1"),
//            TableId.of("p1", "d1", "t2"),
//            TableId.of("p1", "d2", "t1"),
//            TableId.of("p1", "d2", "t2"),
//            TableId.of("p2", "d1", "t1"),
//            TableId.of("p2", "d1", "t2"),
//            TableId.of("p2", "d2", "t1") // unsupported region
//    );
//
//    @Override
//    public String getDatasetLocation(String projectId, String datasetId) throws IOException {
//        if(projectId.equals("p2")&&datasetId.equals("d2"))
//            return "unsupported-region";
//        else
//            return "supported-region";
//    }
//
//    @Override
//    public Stream<TableId> listTables(String projectId, String datasetId) {
//        return
//                tableCatalog.stream()
//                        .filter(t->t.getProject().equals(projectId) && t.getDataset().equals(datasetId));
//    }
//
//    @Override
//    public Stream<DatasetId> listDatasets(String projectId) {
//        return tableCatalog.stream()
//                .filter(t->t.getProject().equals(projectId))
//                .map(t->DatasetId.of(t.getProject(), t.getDataset()))
//                .distinct();
//    }
//
//    @Override
//    public Job submitJob(QueryJobConfiguration queryJobConfiguration) {
//        return null;
//    }
//
//    @Override
//    public TableResult waitAndGetJobResults(Job queryJob) throws InterruptedException, RuntimeException {
//        return null;
//    }
//
//    @Override
//    public List<TableFieldSchema> getTableSchemaFields(String projectId, String datasetId, String tableId) throws IOException {
//        return null;
//    }
//
//    @Override
//    public void patchTable(String projectId, String datasetId, String tableId, List<TableFieldSchema> updatedFields) throws IOException {
//
//    }
//}
