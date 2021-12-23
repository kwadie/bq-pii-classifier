package com.google.cloud.pso.bq_pii_classifier.dispatcher;

import com.google.cloud.pso.bq_pii_classifier.functions.dispatcher.DispatcherConfig;
import com.google.cloud.pso.bq_pii_classifier.helpers.Utils;

public class Environment {

    public DispatcherConfig toConfig(){
        return new DispatcherConfig(
                getProjectId(),
                getComputeRegionId(),
                getDataRegionId(),
                getTaggerTopic()
        );
    }

    public String getProjectId(){
        return Utils.getConfigFromEnv("PROJECT_ID", true);
    }

    public String getComputeRegionId(){
        return Utils.getConfigFromEnv("COMPUTE_REGION_ID", true);
    }

    public String getDataRegionId(){
        return Utils.getConfigFromEnv("DATA_REGION_ID", true);
    }

    public String getTaggerTopic() { return Utils.getConfigFromEnv("TAGGER_TOPIC", true); }

    public String getBqViewFieldsFindings(){
        return Utils.getConfigFromEnv("BQ_VIEW_FIELDS_FINDINGS_SPEC", true);
    }
}
