package com.aiqaos.integration.dto;

public class WorkflowSummary {
    private final String workflowId;
    private final String name;
    private final int totalSteps;
    private final int completedSteps;

    public WorkflowSummary(String workflowId, String name, int totalSteps, int completedSteps) {
        this.workflowId = workflowId;
        this.name = name;
        this.totalSteps = totalSteps;
        this.completedSteps = completedSteps;
    }

    public String getWorkflowId() { return workflowId; }
    public String getName() { return name; }
    public int getTotalSteps() { return totalSteps; }
    public int getCompletedSteps() { return completedSteps; }
}