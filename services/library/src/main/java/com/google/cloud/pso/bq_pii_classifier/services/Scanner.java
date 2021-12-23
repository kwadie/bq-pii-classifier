package com.google.cloud.pso.bq_pii_classifier.services;

import com.google.cloud.pso.bq_pii_classifier.entities.NonRetryableApplicationException;

import java.util.List;

public interface Scanner {


    // list datasets under a project in the format "project.dataset"
    List<String> listDatasets(String project) throws NonRetryableApplicationException, InterruptedException;

    // list tables under a project/dataset in the format "project.dataset.table"
    List<String> listTables(String project, String dataset) throws InterruptedException, NonRetryableApplicationException;
}
