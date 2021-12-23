package com.google.cloud.pso.bq_pii_classifier.entities;

public enum ApplicationLog {
    // Used for generic logging event
    DEFAULT_LOG,
    // Used to log function start/stop
    TRACKER_LOG,
    // Used to log column Level tagging actions
    TAG_HISTORY_LOG,
    // Used to log success dispatched requests per run
    DISPATCHED_REQUESTS_LOG,
    // Used to log failed dispatched requests per run
    FAILED_DISPATCHED_REQUESTS_LOG,
    // To capture trackers with non retryable exceptions during processing
    NON_RETRYABLE_EXCEPTIONS_LOG,
    // To capture trackers with retryable exceptions during processing
    RETRYABLE_EXCEPTIONS_LOG,
}
