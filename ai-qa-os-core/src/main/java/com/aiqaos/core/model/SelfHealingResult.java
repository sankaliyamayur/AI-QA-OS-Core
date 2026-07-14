package com.aiqaos.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SelfHealingResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String healingId;
    private boolean healingApplied;
    private String actionType;
    private String originalFailure;
    private String appliedFix;
    private String recoveryStatus;
    private int retryCount;
    private ExecutionResult originalExecution;
    private ExecutionResult healedExecution;
    private double improvementScore;
    private LocalDateTime createdTime;
    private String failureCategory;
    private String healingStrategy;
    private boolean retrySuccessful;

    public SelfHealingResult() {
        this.createdTime = LocalDateTime.now();
    }

    public String getHealingId() { return healingId; }
    public void setHealingId(String healingId) { this.healingId = healingId; }

    public boolean isHealingApplied() { return healingApplied; }
    public void setHealingApplied(boolean healingApplied) { this.healingApplied = healingApplied; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getOriginalFailure() { return originalFailure; }
    public void setOriginalFailure(String originalFailure) { this.originalFailure = originalFailure; }

    public String getAppliedFix() { return appliedFix; }
    public void setAppliedFix(String appliedFix) { this.appliedFix = appliedFix; }

    public String getRecoveryStatus() { return recoveryStatus; }
    public void setRecoveryStatus(String recoveryStatus) { this.recoveryStatus = recoveryStatus; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public ExecutionResult getOriginalExecution() { return originalExecution; }
    public void setOriginalExecution(ExecutionResult originalExecution) { this.originalExecution = originalExecution; }

    public ExecutionResult getHealedExecution() { return healedExecution; }
    public void setHealedExecution(ExecutionResult healedExecution) { this.healedExecution = healedExecution; }

    public double getImprovementScore() { return improvementScore; }
    public void setImprovementScore(double improvementScore) { this.improvementScore = improvementScore; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public String getFailureCategory() { return failureCategory; }
    public void setFailureCategory(String failureCategory) { this.failureCategory = failureCategory; }

    public String getHealingStrategy() { return healingStrategy; }
    public void setHealingStrategy(String healingStrategy) { this.healingStrategy = healingStrategy; }

    public boolean isRetrySuccessful() { return retrySuccessful; }
    public void setRetrySuccessful(boolean retrySuccessful) { this.retrySuccessful = retrySuccessful; }
}
