package com.google.cloud.pso.bq_pii_classifier.services;


import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pso.bq_pii_classifier.entities.TableOperationRequest;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PubSubServiceImpl implements PubSubService {


    @Override
    public PubSubPublishResults publishTableOperationRequests(String projectId, String topicId, List<TableOperationRequest> requests)
            throws IOException, InterruptedException {

        List<TableOpsRequestSuccessPubSubMessage> successMessages = new ArrayList<>();
        List<TableOpsRequestFailedPubSubMessage> failedMessages = new ArrayList<>();

        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build();

            for (final TableOperationRequest request : requests) {
                ByteString data = ByteString.copyFromUtf8(request.toJsonString());
                PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

                // Once published, returns a server-assigned message id (unique within the topic)
                ApiFuture<String> future = publisher.publish(pubsubMessage);

                // Add an asynchronous callback to handle success / failure
                ApiFutures.addCallback(
                        future,
                        new ApiFutureCallback<String>() {

                            @Override
                            public void onFailure(Throwable throwable) {
                                if (throwable instanceof ApiException) {
                                    ApiException apiException = ((ApiException) throwable);
                                    // details on the API exception
//                                    System.out.println(apiException.getStatusCode().getCode());
//                                    System.out.println(apiException.isRetryable());

                                    failedMessages.add(new TableOpsRequestFailedPubSubMessage(request, apiException));
                                }
                            }

                            @Override
                            public void onSuccess(String messageId) {
                                successMessages.add(new TableOpsRequestSuccessPubSubMessage(request, messageId));
                            }
                        },
                        MoreExecutors.directExecutor());
            }
        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }

        return new PubSubPublishResults(successMessages, failedMessages);
    }
}
