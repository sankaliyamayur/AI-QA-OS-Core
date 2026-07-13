package com.aiqaos.integration.dto;

import java.util.List;

public class PlatformExecutionSummary {
    private String status;
    private long totalDurationMs;
    private WorkflowSummary workflowSummary;
    private List<AgentSummary> agentSummaries;

    public PlatformExecutionSummary(String status, long totalDurationMs, WorkflowSummary workflowSummary, List<AgentSummary> agentSummaries) {
        this.status = status;
        this.totalDurationMs = totalDurationMs;
        this.workflowSummary = workflowSummary;
        this.agentSummaries = agentSummaries;
    }

    public String getStatus() { return status; }
    public long getTotalDurationMs() { return totalDurationMs; }
    public WorkflowSummary getWorkflowSummary() { return workflowSummary; }
    public List<AgentSummary> getAgentSummaries() { return agentSummaries; }
}