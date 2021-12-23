package com.google.cloud.pso.bq_security_classifier.functions.tagger;

public class FunctionOptions {

    public String getDlpJobName() {
        return DlpJobName;
    }

    public void setDlpJobName(String dlpJobName) {
        this.DlpJobName = dlpJobName;
    }

    private String DlpJobName;

    public FunctionOptions(String dlpInspectionJobId) {
        this.DlpJobName = dlpInspectionJobId;
    }

    @Override
    public String toString() {
        return "FunctionOptions{" +
                "dlpJobName='" + DlpJobName + '\'' +
                '}';
    }
}
