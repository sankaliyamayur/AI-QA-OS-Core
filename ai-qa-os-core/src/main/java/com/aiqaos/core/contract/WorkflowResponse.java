package com.aiqaos.core.contract;

import java.util.HashMap;
import java.util.Map;

public class WorkflowResponse extends BaseResponse {
    private String runState;
    private Map<String, Object> outputs = new HashMap<>();

    public String getRunState() { return runState; }
    public void setRunState(String runState) { this.runState = runState; }
    public Map<String, Object> getOutputs() { return outputs; }
    public void setOutputs(Map<String, Object> outputs) { this.outputs = outputs; }
}