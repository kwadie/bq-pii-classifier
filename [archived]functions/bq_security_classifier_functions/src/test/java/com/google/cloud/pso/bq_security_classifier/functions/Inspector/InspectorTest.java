package com.google.cloud.pso.bq_security_classifier.functions.Inspector;


//import static com.google.common.truth.Truth.assertThat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.pso.bq_security_classifier.helpers.TableScanLimitsConfig;
import com.google.cloud.pso.bq_security_classifier.services.BigQueryServiceImpl;
import com.google.cloud.pso.bq_security_classifier.services.DlpServiceImpl;
import com.google.cloud.pso.bq_security_classifier.functions.inspector.Inspector;
import com.google.cloud.pso.bq_security_classifier.functions.inspector.Environment;

import java.io.*;
import java.math.BigInteger;

import com.google.privacy.dlp.v2.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InspectorTest {

    @Mock Environment envMock;

    @Mock DlpServiceImpl dlpServiceMock;

    @Mock BigQueryServiceImpl bqServiceMock;

    @Mock HttpRequest requestMock;

    @Mock HttpResponse responseMock;

    @InjectMocks Inspector function;

    @Test
    public void testInspector() throws IOException {

        String jsonPayLoad = "{\"inputProjectId\":\"targetProject\""
                + ",\"inputDatasetId\":\"targetDataset\""
                + ",\"inputTableId\":\"targetTable\""
                + ",\"trackingId\":\"testTrackingId\""
                + "}";

        // use an empty string as the default request content
        BufferedReader reader = new BufferedReader(new StringReader(jsonPayLoad));
        when(requestMock.getReader()).thenReturn(reader);

        StringWriter responseOut = new StringWriter();
        BufferedWriter writerOut = new BufferedWriter(responseOut);
        when(responseMock.getWriter()).thenReturn(writerOut);

        // set up dlpService Mock
        when(dlpServiceMock.submitJob(any()))
                .thenReturn(DlpJob.newBuilder().setName("testTrackingId").build());

        // set up dlpService Mock
        when(bqServiceMock.getTableNumRows(any(), any(), any()))
                .thenReturn(BigInteger.valueOf(100));

        // set up env Mock
        when(envMock.getProjectId()).thenReturn("serviceProject");
        when(envMock.getRegionId()).thenReturn("serviceRegion");
        when(envMock.getBqResultsDataset()).thenReturn("resultsDataset");
        when(envMock.getBqResultsTable()).thenReturn("resultsTable");
        when(envMock.getDlpNotificationTopic()).thenReturn("dlpTopic");
        when(envMock.getMinLikelihood()).thenReturn("LIKELY");
        when(envMock.getMaxFindings()).thenReturn("50");
        when(envMock.getSamplingMethod()).thenReturn("2");
        when(envMock.getTableScanLimitsJsonConfig()).thenReturn("{\"limitType\": \"NUMBER_OF_ROWS\", \"limits\": {\"5000\": \"500\",\"1000\": \"100\", \"2000\": \"200\"}}");
        when(envMock.getDlpInspectionTemplateId()).thenReturn("dlpTemplate");

        // expected output
        String expectedOutput = "testTrackingId";

        function.service(requestMock, responseMock);
        DlpJob submittedJob = function.getSubmittedDlpJob();
        writerOut.flush();
        assertEquals(expectedOutput, responseOut.toString());


        // check created request
        CreateDlpJobRequest jobRequest = function.getCreateDlpJobRequest();
        assertEquals("testTrackingId", jobRequest.getJobId());
        assertEquals("projects/serviceProject/locations/serviceRegion", jobRequest.getParent());

        InspectJobConfig config = function.getCreateDlpJobRequest().getInspectJob();

        BigQueryOptions targetTable = config.getStorageConfig().getBigQueryOptions();
        assertEquals("targetProject", targetTable.getTableReference().getProjectId());
        assertEquals("targetDataset", targetTable.getTableReference().getDatasetId());
        assertEquals("targetTable", targetTable.getTableReference().getTableId());
        assertEquals(2, targetTable.getSampleMethodValue());
        assertEquals(100, targetTable.getRowsLimit());

        InspectConfig inspectConfig = config.getInspectConfig();
        assertFalse(inspectConfig.getIncludeQuote());
        assertEquals("LIKELY", inspectConfig.getMinLikelihood().name());
        assertEquals(50, inspectConfig.getLimits().getMaxFindingsPerItem());

        // BQ and PubSub actions
        assertEquals(config.getActionsCount(), 2);

        BigQueryTable resultsBqTable = config.getActions(0).getSaveFindings().getOutputConfig().getTable();
        assertEquals("serviceProject", resultsBqTable.getProjectId());
        assertEquals("resultsDataset", resultsBqTable.getDatasetId());
        assertEquals("resultsTable", resultsBqTable.getTableId());

        Action.PublishToPubSub publishToPubSub = config.getActions(1).getPubSub();
        assertEquals("dlpTopic", publishToPubSub.getTopic());

        assertEquals("dlpTemplate", config.getInspectTemplateName());
    }

    @Test
    public void testCreateJob() throws IOException {

        TableScanLimitsConfig limitsConfig = new TableScanLimitsConfig("{\"limitType\": \"NUMBER_OF_ROWS\", \"limits\": {\"5000\": \"500\",\"1000\": \"100\", \"2000\": \"200\"}}")
;
        InspectJobConfig config = Inspector.createJob(
                "targetProject",
                "targetDataset",
                "targetTable",
                2,
                limitsConfig,
                500,
                "LIKELY",
                50,
                "resultsProject",
                "resultsDataset",
                "resultsTable",
                "dlpTopic",
                "dlpTemplate"
        );

        BigQueryOptions targetTable = config.getStorageConfig().getBigQueryOptions();
        assertEquals("targetProject", targetTable.getTableReference().getProjectId());
        assertEquals("targetDataset", targetTable.getTableReference().getDatasetId());
        assertEquals("targetTable", targetTable.getTableReference().getTableId());
        assertEquals(2, targetTable.getSampleMethodValue());
        assertEquals(100, targetTable.getRowsLimit());

        InspectConfig inspectConfig = config.getInspectConfig();
        assertFalse(inspectConfig.getIncludeQuote());
        assertEquals("LIKELY", inspectConfig.getMinLikelihood().name());
        assertEquals(50, inspectConfig.getLimits().getMaxFindingsPerItem());

        // BQ and PubSub actions
        assertEquals(config.getActionsCount(), 2);

        BigQueryTable resultsBqTable = config.getActions(0).getSaveFindings().getOutputConfig().getTable();
        assertEquals("resultsProject", resultsBqTable.getProjectId());
        assertEquals("resultsDataset", resultsBqTable.getDatasetId());
        assertEquals("resultsTable", resultsBqTable.getTableId());

        Action.PublishToPubSub publishToPubSub = config.getActions(1).getPubSub();
        assertEquals("dlpTopic", publishToPubSub.getTopic());

        assertEquals("dlpTemplate", config.getInspectTemplateName());
    }
}