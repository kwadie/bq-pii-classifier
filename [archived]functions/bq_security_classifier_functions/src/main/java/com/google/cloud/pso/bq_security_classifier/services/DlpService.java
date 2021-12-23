package com.google.cloud.pso.bq_security_classifier.services;

import com.google.privacy.dlp.v2.*;

public interface DlpService {

    DlpJob submitJob(CreateDlpJobRequest createDlpJobRequest);

    DlpJob.JobState getJobState(String jobId);

    BigQueryTable getInspectedTable(String jobId);
}
