package com.aiqaos.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExecutionResult {
    private String taskId;
    private String agentId;
    private String executionId;
    private boolean success;
    private String outputData;
    private String errorMessage;
    private LocalDateTime completedAt = LocalDateTime.now();

    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long duration;
    private int passed;
    private int failed;
    private int skipped;
    private List<String> screenshots = new ArrayList<>();
    private String video;
    private String logs;
    private String consoleLogs;
    private String stackTrace;
    private List<String> artifacts = new ArrayList<>();
    private String reportLocation;

    public ExecutionResult() {}

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getOutputData() { return outputData; }
    public void setOutputData(String outputData) { this.outputData = outputData; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public int getPassed() { return passed; }
    public void setPassed(int passed) { this.passed = passed; }

    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }

    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }

    public List<String> getScreenshots() { return screenshots; }
    public void setScreenshots(List<String> screenshots) { this.screenshots = screenshots; }

    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }

    public String getLogs() { return logs; }
    public void setLogs(String logs) { this.logs = logs; }

    public String getConsoleLogs() { return consoleLogs; }
    public void setConsoleLogs(String consoleLogs) { this.consoleLogs = consoleLogs; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public List<String> getArtifacts() { return artifacts; }
    public void setArtifacts(List<String> artifacts) { this.artifacts = artifacts; }

    public String getReportLocation() { return reportLocation; }
    public void setReportLocation(String reportLocation) { this.reportLocation = reportLocation; }
}
