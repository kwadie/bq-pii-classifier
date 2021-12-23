package com.google.cloud.pso.bq_security_classifier.functions.listener;

import com.google.cloud.pso.bq_security_classifier.services.CloudTasksServiceImpl;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.HttpRequest;
import com.google.cloud.tasks.v2.Task;
import com.google.protobuf.ByteString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListenerTest {

    @Mock CloudTasksServiceImpl cloudTasksServiceMock;

    @Mock Environment envMock;

    @InjectMocks Listener function;


    @Test
    public void testListener() throws IOException {

        // Set up mocks
        PubSubMessage pubSubMessageMock = mock(PubSubMessage.class);
        Map<String, String> inputAttributes = new HashMap<>();
        inputAttributes.put("DlpJobName","projects/locations/dlpJobs/i-trackingId");
        when(pubSubMessageMock.getAttributes()).thenReturn(inputAttributes);

        // set env mock
        when(envMock.getProjectId()).thenReturn("project");
        when(envMock.getRegionId()).thenReturn("region");
        when(envMock.getTaggerQueueId()).thenReturn("queue");
        when(envMock.getTaggerTaskServiceAccountEmail()).thenReturn("sa_email");
        when(envMock.getTaggerFunctionHttpEndpoint()).thenReturn("http_endpoint");

        // set cloud tasks service mock
        when(cloudTasksServiceMock.submitTask(any(), anyString()))
                .thenReturn(Task.newBuilder().setName("test-task").build());

        // execute function
        function.accept(pubSubMessageMock, null);

        Task taskRequest = function.getTaskRequest();
        HttpRequest httpRequest = taskRequest.getHttpRequest();

        ByteString expectedPayload = ByteString.copyFrom("{\"dlpJobName\":\"projects/locations/dlpJobs/i-trackingId\"}",
                Charset.defaultCharset());

        assertEquals(expectedPayload, httpRequest.getBody());
        assertEquals("http_endpoint", httpRequest.getUrl());
        assertEquals("sa_email", httpRequest.getOidcToken().getServiceAccountEmail());
    }
}
