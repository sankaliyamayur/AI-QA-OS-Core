package com.aiqaos.observability.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bug_metrics")
public class BugMetricEntity extends BaseEntity {
    private UUID executionId;
    private UUID workflowId;
    private String bugReportId;
    private String failureCategory;
    private String severity;
    private String priority;
    private String rootCause;
    private double confidence;
    private boolean autoDetected = true;
    private String status;
    private LocalDateTime detectedAt;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public String getBugReportId() { return bugReportId; }
    public void setBugReportId(String bugReportId) { this.bugReportId = bugReportId; }
    public String getFailureCategory() { return failureCategory; }
    public void setFailureCategory(String failureCategory) { this.failureCategory = failureCategory; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public boolean isAutoDetected() { return autoDetected; }
    public void setAutoDetected(boolean autoDetected) { this.autoDetected = autoDetected; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
}
