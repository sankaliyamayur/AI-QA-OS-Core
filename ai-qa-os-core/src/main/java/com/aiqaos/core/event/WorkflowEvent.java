package com.aiqaos.core.event;

public class WorkflowEvent extends BaseEvent {
    private String workflowName;
    private String transitionState;

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public String getTransitionState() { return transitionState; }
    public void setTransitionState(String transitionState) { this.transitionState = transitionState; }
}