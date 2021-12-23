package com.google.cloud.pso.bq_security_classifier.functions.inspector;

import com.google.cloud.pso.bq_security_classifier.helpers.Utils;

public class Environment {

    public String getProjectId(){
        return Utils.getConfigFromEnv("PROJECT_ID", true);
    }

    public String getRegionId(){
        return Utils.getConfigFromEnv("REGION_ID", true);
    }

    public String getBqResultsDataset(){
        return Utils.getConfigFromEnv("BQ_RESULTS_DATASET", true);
    }

    public String getBqResultsTable(){
        return Utils.getConfigFromEnv("BQ_RESULTS_TABLE", true);
    }

    public String getDlpNotificationTopic(){
        return Utils.getConfigFromEnv("DLP_NOTIFICATION_TOPIC", true);
    }

    public String getMinLikelihood(){
        return Utils.getConfigFromEnv("MIN_LIKELIHOOD", true);
    }

    public String getMaxFindings(){
        return Utils.getConfigFromEnv("MAX_FINDINGS_PER_ITEM", true);
    }

    public String getSamplingMethod(){
        return Utils.getConfigFromEnv("SAMPLING_METHOD", true);
    }

    public String getDlpInspectionTemplateId(){
        return Utils.getConfigFromEnv("DLP_INSPECTION_TEMPLATE_ID", true);
    }

    public String getTableScanLimitsJsonConfig(){
        return Utils.getConfigFromEnv("TABLE_SCAN_LIMITS_JSON_CONFIG", true);
    }
}
