package com.google.cloud.pso.bq_pii_classifier.tagger;

import com.google.cloud.pso.bq_pii_classifier.functions.tagger.TaggerConfig;
import com.google.cloud.pso.bq_pii_classifier.helpers.Utils;

import java.util.HashSet;

public class Environment {



    public TaggerConfig toConfig (){

        return new TaggerConfig(
                getProjectId(),
                getBqViewFieldsFindings(),
                new HashSet<>(
                        Utils.tokenize(getTaxonomies(), ",", true)),
                getIsDryRun()
        );
    }

    public String getProjectId(){
        return Utils.getConfigFromEnv("PROJECT_ID", true);
    }

    public String getBqViewFieldsFindings(){
        return Utils.getConfigFromEnv("BQ_VIEW_FIELDS_FINDINGS_SPEC", true);
    }

    public String getTaxonomies(){
        return Utils.getConfigFromEnv("TAXONOMIES", true);
    }

    public Boolean getIsDryRun(){
        return Boolean.valueOf(Utils.getConfigFromEnv("IS_DRY_RUN", true));
    }

}
