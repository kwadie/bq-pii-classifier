package com.google.cloud.pso.bq_pii_classifier.services;

import com.google.cloud.bigquery.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BigQueryScannerImpl implements Scanner {

    private BigQuery bqService;

    public BigQueryScannerImpl() throws IOException {

        bqService = BigQueryOptions.getDefaultInstance().getService();
    }

    @Override
    public List<String> listTables(String projectId, String datasetId) {
        return StreamSupport.stream(bqService.listTables(DatasetId.of(projectId, datasetId)).iterateAll().spliterator(),
                false)
                .map(t -> String.format("%s.%s.%s", projectId, datasetId, t.getTableId().getTable()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<String> listDatasets(String projectId) {
        return StreamSupport.stream(bqService.listDatasets(projectId)
                        .iterateAll()
                        .spliterator(),
                false)
                .map(d -> String.format("%s.%s", projectId, d.getDatasetId().getDataset()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
