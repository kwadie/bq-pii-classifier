package com.google.cloud.pso.bq_security_classifier.functions.listener;

import java.util.Map;

public class PubSubMessage {
    public String data;
    public Map<String, String> attributes;
    public String messageId;
    public String publishTime;

    public Map<String, String> getAttributes(){
        return attributes;
    }
}