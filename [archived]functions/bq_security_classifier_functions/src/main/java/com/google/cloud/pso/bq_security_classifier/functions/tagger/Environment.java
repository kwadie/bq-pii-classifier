package com.google.cloud.pso.bq_security_classifier.functions.tagger;

import com.google.cloud.pso.bq_security_classifier.helpers.Utils;

public class Environment {

    public String getProjectId(){
        return Utils.getConfigFromEnv("PROJECT_ID", true);
    }

    public String getDatasetId(){
        return Utils.getConfigFromEnv("DATASET_ID", true);
    }

    public String getBqViewFieldsFindings(){
        return Utils.getConfigFromEnv("BQ_VIEW_FIELDS_FINDINGS", true);
    }

    public String getTaxonomies(){
        return Utils.getConfigFromEnv("TAXONOMIES", true);
    }

    public Boolean getIsDryRun(){
        return Boolean.valueOf(Utils.getConfigFromEnv("IS_DRY_RUN", true));
    }

}
