package com.aiqaos.workflow.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ExecutionHistoryDTO {
    private UUID executionId;
    private UUID workflowId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMs;
    private int totalSteps;
    private int successSteps;
    private int failedSteps;
    private int skippedSteps;
    private int retryCount;
    private double executionCost;
    private long tokenUsage;
    private String gitCommit;
    private String gitBranch;
    private String llmModel;
    private String pipelineVersion;
    private String environment;
    private String browser;
    private String currentStep;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
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
    public String getGitCommit() { return gitCommit; }
    public void setGitCommit(String gitCommit) { this.gitCommit = gitCommit; }
    public String getGitBranch() { return gitBranch; }
    public void setGitBranch(String gitBranch) { this.gitBranch = gitBranch; }
    public String getLlmModel() { return llmModel; }
    public void setLlmModel(String llmModel) { this.llmModel = llmModel; }
    public String getPipelineVersion() { return pipelineVersion; }
    public void setPipelineVersion(String pipelineVersion) { this.pipelineVersion = pipelineVersion; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public static ExecutionHistoryDTO from(com.aiqaos.workflow.entity.WorkflowExecutionEntity entity) {
        ExecutionHistoryDTO dto = new ExecutionHistoryDTO();
        dto.setExecutionId(entity.getExecutionId());
        dto.setWorkflowId(entity.getWorkflowId());
        dto.setStatus(entity.getStatus());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setDurationMs(entity.getDuration());
        dto.setTotalSteps(entity.getTotalSteps());
        dto.setSuccessSteps(entity.getSuccessSteps());
        dto.setFailedSteps(entity.getFailedSteps());
        dto.setSkippedSteps(entity.getSkippedSteps());
        dto.setRetryCount(entity.getRetryCount());
        dto.setExecutionCost(entity.getExecutionCost());
        dto.setTokenUsage(entity.getTokenUsage());
        dto.setGitCommit(entity.getGitCommit());
        dto.setGitBranch(entity.getGitBranch());
        dto.setLlmModel(entity.getLlmModel());
        dto.setPipelineVersion(entity.getPipelineVersion());
        dto.setEnvironment(entity.getEnvironment());
        dto.setBrowser(entity.getBrowser());
        dto.setCurrentStep(entity.getCurrentStep());
        return dto;
    }
}
