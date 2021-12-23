package com.google.cloud.pso.bq_pii_classifier.functions.tagger;

import java.util.Set;

public class TaggerConfig {

    private String projectId;
    private String bqViewFieldsFindings;
    private Set<String> appOwnedTaxonomies;
    private Boolean isDryRun;

    public TaggerConfig(String projectId, String bqViewFieldsFindings, Set<String> appOwnedTaxonomies, Boolean isDryRun) {
        this.projectId = projectId;
        this.bqViewFieldsFindings = bqViewFieldsFindings;
        this.appOwnedTaxonomies = appOwnedTaxonomies;
        this.isDryRun = isDryRun;
    }

    public String getBqViewFieldsFindings() {
        return bqViewFieldsFindings;
    }

    public  Set<String> getAppOwnedTaxonomies() {
        return appOwnedTaxonomies;
    }

    public Boolean getDryRun() {
        return isDryRun;
    }

    public String getProjectId() {
        return projectId;
    }

    @Override
    public String toString() {
        return "TaggerConfig{" +
                "projectId='" + projectId + '\'' +
                ", bqViewFieldsFindings='" + bqViewFieldsFindings + '\'' +
                ", appOwnedTaxonomies=" + appOwnedTaxonomies +
                ", isDryRun=" + isDryRun +
                '}';
    }
}
