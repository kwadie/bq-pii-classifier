package com.google.cloud.pso.bq_security_classifier.functions.tagger;

public enum ColumnTaggingAction {

    // keep existing policy tag
    // e.g. keep existing manual tagging from an external taxonomy
    KEEP_EXISTING,
    // Overwrite the existing policy tag
    // e.g. previous run detected as STREET_ADDRESS and now as PERSON_NAME (across solution-managed taxonomies)
    OVERWRITE,
    // No change detected in policy tags
    NO_CHANGE,
    // Apply a policy tag to a column without existing tags
    CREATE,

    // Same action logic but without applying the tags to columns (only for logging)
    DRY_RUN_KEEP_EXISTING,
    DRY_RUN_OVERWRITE,
    DRY_RUN_NO_CHANGE,
    DRY_RUN_CREATE
}
