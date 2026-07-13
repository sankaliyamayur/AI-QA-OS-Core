package com.aiqaos.observability.model;

public class AlertDTO {
    private String ruleName;
    private String severity;

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}