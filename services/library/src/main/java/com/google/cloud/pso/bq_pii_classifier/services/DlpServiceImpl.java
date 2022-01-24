package com.google.cloud.pso.bq_pii_classifier.services;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;

import java.io.IOException;

public class DlpServiceImpl implements DlpService {

    DlpServiceClient dlpServiceClient;

    public DlpServiceImpl () throws IOException {
        dlpServiceClient = DlpServiceClient.create();
    }

    @Override
    public void shutDown(){
        dlpServiceClient.shutdown();
    }

    @Override
    public DlpJob submitJob(CreateDlpJobRequest createDlpJobRequest){
        return dlpServiceClient.createDlpJob(createDlpJobRequest);
    }

    @Override
    public DlpJob.JobState getJobState(String jobId){
        return dlpServiceClient.getDlpJob(jobId).getState();
    }
    @Override
    public BigQueryTable getInspectedTable(String jobId){
        return dlpServiceClient.getDlpJob(jobId)
                .getInspectDetails()
                .getRequestedOptions()
                .getJobConfig()
                .getStorageConfig()
                .getBigQueryOptions()
                .getTableReference();
    }
}
