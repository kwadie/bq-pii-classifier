package com.google.cloud.pso.bq_security_classifier.functions.dispatcher;


import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.pso.bq_security_classifier.functions.dispatcher.Environment;
import com.google.cloud.pso.bq_security_classifier.functions.inspector.Inspector;
import com.google.cloud.pso.bq_security_classifier.services.*;
import com.google.cloud.tasks.v2.Task;
import com.google.common.collect.Lists;
import com.google.privacy.dlp.v2.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherTest {

    @Mock CloudTasksServiceImpl cloudTasksServiceMock;
    @Mock Environment envMock;
    @Mock BigQueryServiceImpl bqServiceMock;

    @Mock HttpRequest requestMock;
    @Mock HttpResponse responseMock;

    @InjectMocks Dispatcher function;

    @Before
    public void setUp() throws IOException {
        // CloudTasksServiceImpl cloudTasksServiceMock = mock(CloudTasksServiceImpl.class);

        // Environment envMock = mock(Environment.class);

        // mock cloud tasks service
        when(cloudTasksServiceMock.submitTask(any(), anyString()))
                .thenReturn(Task.newBuilder().setName("test-task").build());

        // mock bqService
        // use lenient() to disable strict stubbing. Mockito detects that the stubs are not used by they actually are!
        //BigQueryServiceImpl bqServiceMock = mock(BigQueryServiceImpl.class);
        lenient().when(bqServiceMock.listDatasets("p1")).thenReturn(
                Stream.of(DatasetId.of("p1","d1"), DatasetId.of("p1","d2")));
        lenient().when(bqServiceMock.listDatasets("p2")).thenReturn(
                Stream.of(DatasetId.of("p2","d1"), DatasetId.of("p2","d2")));

        // list p1 tables
        lenient().when(bqServiceMock.listTables("p1", "d1")).thenReturn(
                Stream.of(TableId.of("p1","d1","t1"), TableId.of("p1","d1","t2")));

        lenient().when(bqServiceMock.listTables("p1", "d2")).thenReturn(
                Stream.of(TableId.of("p1","d2","t1"), TableId.of("p1","d2","t2")));

        // list p2 tables
        lenient().when(bqServiceMock.listTables("p2", "d1")).thenReturn(
                Stream.of(TableId.of("p2","d1","t1"), TableId.of("p2","d1","t2")));
        lenient().when(bqServiceMock.listTables("p2", "d2")).thenReturn(
                Stream.of(TableId.of("p2","d2","t1")));

        lenient().when(bqServiceMock.getDatasetLocation(anyString(), anyString())).thenReturn("supported-region");
        lenient().when(bqServiceMock.getDatasetLocation("p2", "d2")).thenReturn("unsupported-region");

    }

    @Test
    public void testDispatcher_withTables () throws IOException {

        String jsonPayLoad = "{\"tablesInclude\":\"p1.d1.t1, p1.d1.t2\""
                + ",\"tablesExclude\":\"\""
                + ",\"datasetsInclude\":\"p1.d2\""  // should have no effect
                + ",\"datasetsExclude\":\"\""
                + ",\"projectsInclude\":\"p1, p2\""
                + "}";

        List<String> expectedOutput = Lists.newArrayList("p1.d1.t1", "p1.d1.t2");
        List<String> actualOutput = testWithInput(jsonPayLoad);

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testDispatcher_withDatasets () throws IOException {

        String jsonPayLoad = "{\"tablesInclude\":\"\""
                + ",\"tablesExclude\":\"p1.d1.t1\""
                + ",\"datasetsInclude\":\"p1.d1, p1.d2\""
                + ",\"datasetsExclude\":\"\""
                + ",\"projectsInclude\":\"p2\"" // should have no effect
                + "}";

        List<String> expectedOutput = Lists.newArrayList("p1.d1.t2", "p1.d2.t1", "p1.d2.t2");
        List<String> actualOutput = testWithInput(jsonPayLoad);

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testDispatcher_withProjects () throws IOException {

        String jsonPayLoad = "{\"tablesInclude\":\"\""
                + ",\"tablesExclude\":\"p1.d2.t1\""
                + ",\"datasetsInclude\":\"\""
                + ",\"datasetsExclude\":\"p1.d1\""
                + ",\"projectsInclude\":\"p1, p2\"" // should have no effect
                + "}";

        List<String> expectedOutput = Lists.newArrayList("p1.d2.t2", "p2.d1.t1", "p2.d1.t2");
        List<String> actualOutput = testWithInput(jsonPayLoad);

        assertEquals(expectedOutput, actualOutput);
    }

    private List<String> testWithInput (String payload) throws IOException {

        // use an empty string as the default request content
        //HttpRequest requestMock = mock(HttpRequest.class);
        //HttpResponse responseMock = mock(HttpResponse.class);
        BufferedReader reader = new BufferedReader(new StringReader(payload));
        when(requestMock.getReader()).thenReturn(reader);

        StringWriter responseOut = new StringWriter();
        BufferedWriter writerOut = new BufferedWriter(responseOut);
        when(responseMock.getWriter()).thenReturn(writerOut);

        // set up env Mock
        when(envMock.getProjectId()).thenReturn("serviceProject");
        when(envMock.getRegionId()).thenReturn("supported-region");
        when(envMock.getInspectorTaskQueue()).thenReturn("inspectorQueue");
        when(envMock.getInspectorTaskServiceAccountEmail()).thenReturn("sa");
        when(envMock.getInspectorFunctionHttpEndpoint()).thenReturn("http://inspector");

        //Dispatcher function = new Dispatcher(envMock, bqServiceMock, cloudTasksServiceMock);
        function.service(requestMock, responseMock);

        return function.getInspectedTables();
    }
}