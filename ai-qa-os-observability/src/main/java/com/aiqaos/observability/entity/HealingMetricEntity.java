package com.aiqaos.observability.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "healing_metrics")
public class HealingMetricEntity extends BaseEntity {
    private String healingId;
    private UUID executionId;
    private UUID workflowId;
    private String failureCategory;
    private String actionType;
    private String healingStrategy;
    private int retryCount;
    private boolean healingApplied;
    private boolean retrySuccessful;
    private String recoveryStatus;
    private double improvementScore;
    private String appliedFix;
    private LocalDateTime recordedAt;

    public String getHealingId() { return healingId; }
    public void setHealingId(String healingId) { this.healingId = healingId; }
    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public String getFailureCategory() { return failureCategory; }
    public void setFailureCategory(String failureCategory) { this.failureCategory = failureCategory; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getHealingStrategy() { return healingStrategy; }
    public void setHealingStrategy(String healingStrategy) { this.healingStrategy = healingStrategy; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public boolean isHealingApplied() { return healingApplied; }
    public void setHealingApplied(boolean healingApplied) { this.healingApplied = healingApplied; }
    public boolean isRetrySuccessful() { return retrySuccessful; }
    public void setRetrySuccessful(boolean retrySuccessful) { this.retrySuccessful = retrySuccessful; }
    public String getRecoveryStatus() { return recoveryStatus; }
    public void setRecoveryStatus(String recoveryStatus) { this.recoveryStatus = recoveryStatus; }
    public double getImprovementScore() { return improvementScore; }
    public void setImprovementScore(double improvementScore) { this.improvementScore = improvementScore; }
    public String getAppliedFix() { return appliedFix; }
    public void setAppliedFix(String appliedFix) { this.appliedFix = appliedFix; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
