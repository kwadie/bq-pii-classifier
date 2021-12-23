package com.google.cloud.pso.bq_security_classifier.functions.dispatcher;

import com.google.cloud.pso.bq_security_classifier.helpers.Utils;

public class Environment {

    public String getProjectId(){
        return Utils.getConfigFromEnv("PROJECT_ID", true);
    }

    public String getRegionId(){
        return Utils.getConfigFromEnv("REGION_ID", true);
    }

    public String getInspectorTaskQueue(){
        return Utils.getConfigFromEnv("QUEUE_ID", true);
    }

    public String getInspectorTaskServiceAccountEmail(){
        return Utils.getConfigFromEnv("SA_EMAIL", true);
    }

    public String getInspectorFunctionHttpEndpoint(){
        return Utils.getConfigFromEnv("HTTP_ENDPOINT", true);
    }
}
