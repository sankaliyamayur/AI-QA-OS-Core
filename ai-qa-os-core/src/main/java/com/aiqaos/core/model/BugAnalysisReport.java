package com.aiqaos.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BugAnalysisReport {
    private String reportId;
    private String failureReason;
    private String stackTrace;
    private List<String> suspectedRootCauses = new ArrayList<>();
    private String recommendation;
    private boolean isSelfHealable;

    private String rootCause;
    private String failureCategory;
    private String impactedComponent;
    private double confidence;
    private String selfHealingSuggestion;
    private boolean requiresRegeneration;
    private List<String> failedTestCases = new ArrayList<>();
    private List<String> affectedScripts = new ArrayList<>();
    private String stackTraceSummary;
    private LocalDateTime createdTime = LocalDateTime.now();
    private String severity;
    private String priority;
    private String status;

    public BugAnalysisReport() {}

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public List<String> getSuspectedRootCauses() { return suspectedRootCauses; }
    public void setSuspectedRootCauses(List<String> suspectedRootCauses) { this.suspectedRootCauses = suspectedRootCauses; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public boolean isSelfHealable() { return isSelfHealable; }
    public void setSelfHealable(boolean selfHealable) { isSelfHealable = selfHealable; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getFailureCategory() { return failureCategory; }
    public void setFailureCategory(String failureCategory) { this.failureCategory = failureCategory; }

    public String getImpactedComponent() { return impactedComponent; }
    public void setImpactedComponent(String impactedComponent) { this.impactedComponent = impactedComponent; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getSelfHealingSuggestion() { return selfHealingSuggestion; }
    public void setSelfHealingSuggestion(String selfHealingSuggestion) { this.selfHealingSuggestion = selfHealingSuggestion; }

    public boolean isRequiresRegeneration() { return requiresRegeneration; }
    public void setRequiresRegeneration(boolean requiresRegeneration) { this.requiresRegeneration = requiresRegeneration; }

    public List<String> getFailedTestCases() { return failedTestCases; }
    public void setFailedTestCases(List<String> failedTestCases) { this.failedTestCases = failedTestCases; }

    public List<String> getAffectedScripts() { return affectedScripts; }
    public void setAffectedScripts(List<String> affectedScripts) { this.affectedScripts = affectedScripts; }

    public String getStackTraceSummary() { return stackTraceSummary; }
    public void setStackTraceSummary(String stackTraceSummary) { this.stackTraceSummary = stackTraceSummary; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
