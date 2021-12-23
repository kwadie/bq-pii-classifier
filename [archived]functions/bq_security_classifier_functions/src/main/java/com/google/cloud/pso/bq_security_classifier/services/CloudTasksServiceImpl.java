package com.google.cloud.pso.bq_security_classifier.services;

import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.charset.Charset;

public class CloudTasksServiceImpl implements CloudTasksService {

    private CloudTasksClient client;

    public CloudTasksServiceImpl() throws IOException {
        client = CloudTasksClient.create();
    }

    @Override
    public Task submitTask(Task task, String queuePath){

        // Send create task request.
        return client.createTask(queuePath, task);
    }
}
