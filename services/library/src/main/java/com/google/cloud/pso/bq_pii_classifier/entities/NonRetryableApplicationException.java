package com.google.cloud.pso.bq_pii_classifier.entities;

public class NonRetryableApplicationException extends Exception {
    public NonRetryableApplicationException(String msg){
        super(msg);
    }
}
