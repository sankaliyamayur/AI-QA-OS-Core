package com.aiqaos.workflow.model;

import java.util.ArrayList;
import java.util.List;

public class ExecutionComparisonDTO {
    private ExecutionHistoryDTO executionA;
    private ExecutionHistoryDTO executionB;
    private long durationDeltaMs;
    private double costDelta;
    private long tokenDelta;
    private int failedStepsDelta;
    private List<String> notes = new ArrayList<>();

    public ExecutionHistoryDTO getExecutionA() { return executionA; }
    public void setExecutionA(ExecutionHistoryDTO executionA) { this.executionA = executionA; }
    public ExecutionHistoryDTO getExecutionB() { return executionB; }
    public void setExecutionB(ExecutionHistoryDTO executionB) { this.executionB = executionB; }
    public long getDurationDeltaMs() { return durationDeltaMs; }
    public void setDurationDeltaMs(long durationDeltaMs) { this.durationDeltaMs = durationDeltaMs; }
    public double getCostDelta() { return costDelta; }
    public void setCostDelta(double costDelta) { this.costDelta = costDelta; }
    public long getTokenDelta() { return tokenDelta; }
    public void setTokenDelta(long tokenDelta) { this.tokenDelta = tokenDelta; }
    public int getFailedStepsDelta() { return failedStepsDelta; }
    public void setFailedStepsDelta(int failedStepsDelta) { this.failedStepsDelta = failedStepsDelta; }
    public List<String> getNotes() { return notes; }
    public void setNotes(List<String> notes) { this.notes = notes; }
}
