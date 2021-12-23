package com.google.cloud.pso.bq_security_classifier.functions.tagger;

import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.pso.bq_security_classifier.services.BigQueryService;
import com.google.cloud.pso.bq_security_classifier.services.BigQueryServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TaggerHelper {

    private BigQueryService bqService;

    public TaggerHelper (BigQueryService bqService) throws IOException {
        this.bqService = bqService;
    }

    public Map<String, String> getFieldsToPolicyTagsMap(String projectId,
                                                        String datasetId,
                                                        String filedsToInfoTypeFindingsView,
                                                        String dlpJobName) throws InterruptedException {

        // This view is defined in Terraform based on the dlp results table. If it needs update, do it in Terraform
        String queryTemplate = "SELECT field_name, policy_tag FROM `%s.%s.%s` WHERE job_name = '%s' ";

        String formattedQuery = String.format(queryTemplate,
                projectId,
                datasetId,
                filedsToInfoTypeFindingsView,
                dlpJobName
        );

        QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(formattedQuery)
                        .setUseLegacySql(false)
                        .build();

        // Create a job ID so that we can safely retry.
        Job queryJob = bqService.submitJob(queryConfig);

        TableResult result = bqService.waitAndGetJobResults(queryJob);

        // Construct a mapping between field names and DLP infotypes
        Map<String, String> fieldsToPolicyTagMap = new HashMap<>();
        for (FieldValueList row : result.iterateAll()) {
                String fieldName = row.get("field_name").isNull()? "" : row.get("field_name").getStringValue();
                String infoTypeName = row.get("policy_tag").isNull()? "": row.get("policy_tag").getStringValue();
                fieldsToPolicyTagMap.put(fieldName, infoTypeName);
        }

        return fieldsToPolicyTagMap;
    }
}
