package com.google.cloud.pso.bq_security_classifier.functions.tagger;

import com.google.api.client.util.Lists;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.PolicyTags;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.pso.bq_security_classifier.functions.inspector.Inspector;
import com.google.cloud.pso.bq_security_classifier.helpers.Utils;
import com.google.cloud.pso.bq_security_classifier.services.BigQueryService;
import com.google.cloud.pso.bq_security_classifier.services.DlpService;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.DlpJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaggerTest {

    @Mock Environment envMock;
    @Mock DlpService dlpServiceMock;
    @Mock BigQueryService bigQueryServiceMock;
    @Mock TaggerHelper taggerHelper;

    @Mock
    HttpRequest requestMock;

    @Mock
    HttpResponse responseMock;

    @InjectMocks
    Tagger function;

    @Test
    public void testTagger() throws IOException, InterruptedException {

        String jsonPayLoad = "{\"dlpJobName\":\"dlpJobId\"}";

        // use an empty string as the default request content
        BufferedReader reader = new BufferedReader(new StringReader(jsonPayLoad));
        when(requestMock.getReader()).thenReturn(reader);

        StringWriter responseOut = new StringWriter();
        BufferedWriter writerOut = new BufferedWriter(responseOut);
        when(responseMock.getWriter()).thenReturn(writerOut);

        // Mock Dlp service
        when(dlpServiceMock.getJobState("dlpJobId"))
                .thenReturn(DlpJob.JobState.DONE);
        when(dlpServiceMock.getInspectedTable("dlpJobId"))
                .thenReturn(BigQueryTable.newBuilder()
                        .setProjectId("targetProject")
                        .setDatasetId("targetDataset")
                        .setTableId("targetTable")
                        .build()
                );

        // Mock Env
        // env variables are not used by Mocks
//        when(envMock.getProjectId()).thenReturn("serviceProject");
//        when(envMock.getDatasetId()).thenReturn("resultsDataset");
//        when(envMock.getDlpResultsTable()).thenReturn("resultsTable");
//        when(envMock.getBqViewFieldsFindings()).thenReturn("bqView");
          when(envMock.getIsDryRun()).thenReturn(Boolean.TRUE);
          when(envMock.getTaxonomies()).thenReturn("auto_taxonomy_1, auto_taxonomy_2");


        // Mock Bq service
        when(bigQueryServiceMock.getTableSchemaFields("targetProject", "targetDataset", "targetTable"))
                .thenReturn(
                Arrays.asList(
                        new TableFieldSchema().setName("email")
                                .setPolicyTags(new TableFieldSchema.PolicyTags().setNames(Arrays.asList("auto_taxonomy_1/email"))),
                        new TableFieldSchema().setName("phone")
                                .setPolicyTags(new TableFieldSchema.PolicyTags().setNames(Arrays.asList("auto_taxonomy_1/phone"))),
                        new TableFieldSchema().setName("address")
                                .setPolicyTags(new TableFieldSchema.PolicyTags().setNames(Arrays.asList("manual_taxonomy/address"))),
                        new TableFieldSchema().setName("non_conf")
        ));


        // Mock TaggerHelper
        Map<String, String> fieldsToPolicyTagsMap = new HashMap<>();
        fieldsToPolicyTagsMap.put("email", "auto_taxonomy_2/email_new");
        fieldsToPolicyTagsMap.put("phone", "auto_taxonomy_1/phone");
        fieldsToPolicyTagsMap.put("address", "auto_taxonomy_1/address");
        when(taggerHelper.getFieldsToPolicyTagsMap(any(), any(), any(), any())).thenReturn(fieldsToPolicyTagsMap);


        Map<String, String> expectedFieldsAndPolicyTags = new HashMap<>();
        expectedFieldsAndPolicyTags.put("email", "auto_taxonomy_2/email_new"); // overwrite when belongs to app-managed taxonomies
        expectedFieldsAndPolicyTags.put("phone", "auto_taxonomy_1/phone"); // overwrite when belongs to app-managed taxonomies
        expectedFieldsAndPolicyTags.put("address", "manual_taxonomy/address"); // keep non app-managed taxonomies
        expectedFieldsAndPolicyTags.put("non_conf", ""); // no tags

        function.service(requestMock, responseMock);
        Map<String, String> newFieldsAndPolicyTags = function.getFinalFieldsToPolicyTags();

        assertEquals(expectedFieldsAndPolicyTags, newFieldsAndPolicyTags);
    }
}
