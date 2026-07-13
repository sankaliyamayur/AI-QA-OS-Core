package com.aiqaos.workflow.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_executions")
public class WorkflowExecutionEntity extends BaseEntity {

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private long duration;

    @Column(name = "result")
    private String result;

    @Column(name = "total_steps")
    private int totalSteps;

    @Column(name = "success_steps")
    private int successSteps;

    @Column(name = "failed_steps")
    private int failedSteps;

    @Column(name = "skipped_steps")
    private int skippedSteps;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "execution_cost")
    private double executionCost;

    @Column(name = "token_usage")
    private long tokenUsage;

    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }
    public int getSuccessSteps() { return successSteps; }
    public void setSuccessSteps(int successSteps) { this.successSteps = successSteps; }
    public int getFailedSteps() { return failedSteps; }
    public void setFailedSteps(int failedSteps) { this.failedSteps = failedSteps; }
    public int getSkippedSteps() { return skippedSteps; }
    public void setSkippedSteps(int skippedSteps) { this.skippedSteps = skippedSteps; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public double getExecutionCost() { return executionCost; }
    public void setExecutionCost(double executionCost) { this.executionCost = executionCost; }
    public long getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(long tokenUsage) { this.tokenUsage = tokenUsage; }
}