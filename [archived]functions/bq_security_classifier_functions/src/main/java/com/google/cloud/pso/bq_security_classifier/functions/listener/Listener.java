package com.google.cloud.pso.bq_security_classifier.functions.listener;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.pso.bq_security_classifier.helpers.LoggingHelper;
import com.google.cloud.pso.bq_security_classifier.helpers.Utils;
import com.google.cloud.pso.bq_security_classifier.services.CloudTasksService;
import com.google.cloud.pso.bq_security_classifier.services.CloudTasksServiceImpl;
import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.charset.Charset;


public class Listener implements BackgroundFunction<PubSubMessage> {

    private final LoggingHelper logger = new LoggingHelper(
            Listener.class.getSimpleName(),
            applicationName,
            defaultLog,
            trackerLog,
            functionNumber);
    private static final String applicationName = "[bq-security-classifier]";
    private static final String defaultLog = "default-log";
    private static final String trackerLog = "tracker-log";
    private static final Integer functionNumber = 3;

    private CloudTasksService cloudTasksService;
    private Environment environment;

    private Task taskRequest;
    private Task submittedTask;

    public Task getSubmittedTask(){
        return this.submittedTask;
    }

    public Task getTaskRequest(){
        return this.taskRequest;
    }

    public Listener() throws IOException {
        cloudTasksService = new CloudTasksServiceImpl();
        environment = new Environment();
    }

    @Override
    public void accept(PubSubMessage pubSubMessage, Context context) throws IOException {

        String dlpJobName = pubSubMessage.getAttributes().getOrDefault("DlpJobName", "");

        if(dlpJobName.isBlank()){
            throw new IllegalArgumentException("PubSub message attribute 'DlpJobName' is missing.");
        }

        // dlp job is created using the trackingId via the Inspector CF
        String trackingId = Utils.extractTrackingIdFromJobName(dlpJobName);

        logger.logFunctionStart(trackingId);

        logger.logInfoWithTracker(trackingId, String.format("Parsed DlpJobName %s", dlpJobName));

        String payload = String.format("{\"dlpJobName\":\"%s\"}", dlpJobName);

        // Construct the fully qualified queue name.
        String queuePath = QueueName.of(environment.getProjectId(),
                environment.getRegionId(),
                environment.getTaggerQueueId()).toString();

        OidcToken oidcToken = OidcToken.newBuilder()
                .setServiceAccountEmail(environment.getTaggerTaskServiceAccountEmail())
                .build();

        // Construct the task body.
        taskRequest =
                Task.newBuilder()
                        .setHttpRequest(
                                HttpRequest.newBuilder()
                                        .setBody(ByteString.copyFrom(payload, Charset.defaultCharset()))
                                        .setUrl(environment.getTaggerFunctionHttpEndpoint())
                                        .setHttpMethod(HttpMethod.POST)
                                        .setOidcToken(oidcToken)
                                        .build()).build();

        // Send create task request.
        submittedTask = cloudTasksService.submitTask(taskRequest, queuePath);

        logger.logInfoWithTracker(trackingId, String.format("Task created: %s", submittedTask.getName()));

        logger.logFunctionEnd(trackingId);
    }


}