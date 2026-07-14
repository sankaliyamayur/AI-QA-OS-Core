package com.aiqaos.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RecoveryAttempt implements Serializable {
    private static final long serialVersionUID = 1L;

    private String attemptId;
    private String executionId;
    private String strategy;
    private String status;
    private String errorBefore;
    private String resultAfter;
    private int attemptNumber;
    private LocalDateTime timestamp;

    public RecoveryAttempt() {
        this.timestamp = LocalDateTime.now();
    }

    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorBefore() { return errorBefore; }
    public void setErrorBefore(String errorBefore) { this.errorBefore = errorBefore; }

    public String getResultAfter() { return resultAfter; }
    public void setResultAfter(String resultAfter) { this.resultAfter = resultAfter; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
