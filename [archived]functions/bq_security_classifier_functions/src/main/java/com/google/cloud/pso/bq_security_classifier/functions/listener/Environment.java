package com.google.cloud.pso.bq_security_classifier.functions.listener;

import com.google.cloud.pso.bq_security_classifier.helpers.Utils;

public class Environment {

    public String getProjectId(){
        return Utils.getConfigFromEnv("PROJECT_ID", true);
    }

    public String getRegionId(){
        return Utils.getConfigFromEnv("REGION_ID", true);
    }

    public String getTaggerQueueId(){
        return Utils.getConfigFromEnv("QUEUE_ID", true);
    }

    public String getTaggerTaskServiceAccountEmail(){
        return Utils.getConfigFromEnv("SA_EMAIL", true);
    }

    public String getTaggerFunctionHttpEndpoint(){
        return Utils.getConfigFromEnv("HTTP_ENDPOINT", true);
    }
}