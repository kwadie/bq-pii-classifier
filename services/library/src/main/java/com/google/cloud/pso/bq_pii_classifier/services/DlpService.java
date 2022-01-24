package com.google.cloud.pso.bq_pii_classifier.services;

import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;

public interface DlpService {

    DlpJob submitJob(CreateDlpJobRequest createDlpJobRequest);

    DlpJob.JobState getJobState(String jobId);

    BigQueryTable getInspectedTable(String jobId);

    void shutDown();
}
