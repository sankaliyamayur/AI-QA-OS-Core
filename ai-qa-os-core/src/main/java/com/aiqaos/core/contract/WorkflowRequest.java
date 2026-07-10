package com.aiqaos.core.contract;

import java.util.HashMap;
import java.util.Map;

public class WorkflowRequest extends BaseRequest {
    private String workflowName;
    private Map<String, Object> inputs = new HashMap<>();

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public Map<String, Object> getInputs() { return inputs; }
    public void setInputs(Map<String, Object> inputs) { this.inputs = inputs; }
}