package com.aiqaos.gateway.dto;

import java.util.Map;

public class WorkflowStartRequestDTO extends GatewayRequestDTO {
    private String workflowName;
    private Map<String, Object> parameters;

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}