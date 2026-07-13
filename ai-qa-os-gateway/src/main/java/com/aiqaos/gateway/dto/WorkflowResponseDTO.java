package com.aiqaos.gateway.dto;

public class WorkflowResponseDTO extends GatewayResponseDTO {
    private String workflowId;
    private String workflowStatus;

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getWorkflowStatus() { return workflowStatus; }
    public void setWorkflowStatus(String workflowStatus) { this.workflowStatus = workflowStatus; }
}