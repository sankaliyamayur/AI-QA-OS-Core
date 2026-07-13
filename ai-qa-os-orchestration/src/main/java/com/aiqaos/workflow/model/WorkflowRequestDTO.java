package com.aiqaos.workflow.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkflowRequestDTO implements BaseDTO {
    private UUID workflowId;
    private Map<String, Object> inputParameters = new HashMap<>();

    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public Map<String, Object> getInputParameters() { return inputParameters; }
    public void setInputParameters(Map<String, Object> inputParameters) { this.inputParameters = inputParameters; }
}