package com.google.cloud.pso.bq_pii_classifier.listener;

import com.google.cloud.pso.bq_pii_classifier.functions.listener.Listener;
import com.google.cloud.pso.bq_pii_classifier.functions.listener.ListenerConfig;
import com.google.cloud.pso.bq_pii_classifier.functions.tagger.TaggerConfig;
import com.google.cloud.pso.bq_pii_classifier.helpers.Utils;

import java.util.HashSet;

public class Environment {



    public ListenerConfig toConfig (){

        return new ListenerConfig(
                getProjectId(),
                getRegionId(),
                getTaggerTopicId()
        );
    }

    public String getProjectId(){
        return Utils.getConfigFromEnv("PROJECT_ID", true);
    }

    public String getRegionId(){
        return Utils.getConfigFromEnv("REGION_ID", true);
    }

    public String getTaggerTopicId(){
        return Utils.getConfigFromEnv("TAGGER_TOPIC_ID", true);
    }


}
