package com.google.cloud.pso.bq_pii_classifier.services;

import com.google.cloud.pso.bq_pii_classifier.entities.TableOperationRequest;

import java.io.IOException;
import java.util.List;

public interface PubSubService {

    //public PubSubPublishResults publishMessages(String projectId, String topicId, List<String> messages) throws IOException, InterruptedException;

    public PubSubPublishResults publishTableOperationRequests(String projectId, String topicId, List<TableOperationRequest> requests) throws IOException, InterruptedException;
}
