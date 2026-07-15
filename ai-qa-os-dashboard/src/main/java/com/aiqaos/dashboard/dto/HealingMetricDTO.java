package com.aiqaos.dashboard.dto;

import com.aiqaos.observability.entity.HealingMetricEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public class HealingMetricDTO {
    private UUID id;
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

    public static HealingMetricDTO from(HealingMetricEntity entity) {
        HealingMetricDTO dto = new HealingMetricDTO();
        dto.id = entity.getId();
        dto.healingId = entity.getHealingId();
        dto.executionId = entity.getExecutionId();
        dto.workflowId = entity.getWorkflowId();
        dto.failureCategory = entity.getFailureCategory();
        dto.actionType = entity.getActionType();
        dto.healingStrategy = entity.getHealingStrategy();
        dto.retryCount = entity.getRetryCount();
        dto.healingApplied = entity.isHealingApplied();
        dto.retrySuccessful = entity.isRetrySuccessful();
        dto.recoveryStatus = entity.getRecoveryStatus();
        dto.improvementScore = entity.getImprovementScore();
        dto.appliedFix = entity.getAppliedFix();
        dto.recordedAt = entity.getRecordedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public String getHealingId() { return healingId; }
    public UUID getExecutionId() { return executionId; }
    public UUID getWorkflowId() { return workflowId; }
    public String getFailureCategory() { return failureCategory; }
    public String getActionType() { return actionType; }
    public String getHealingStrategy() { return healingStrategy; }
    public int getRetryCount() { return retryCount; }
    public boolean isHealingApplied() { return healingApplied; }
    public boolean isRetrySuccessful() { return retrySuccessful; }
    public String getRecoveryStatus() { return recoveryStatus; }
    public double getImprovementScore() { return improvementScore; }
    public String getAppliedFix() { return appliedFix; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
