package com.aiqaos.core.context;

import com.aiqaos.core.contract.BaseContext;
import java.util.HashMap;
import java.util.Map;

public class AgentContext extends BaseContext {
    private Map<String, Object> promptContext = new HashMap<>();
    private Map<String, Object> workflowContext = new HashMap<>();
    private Map<String, Object> memoryContext = new HashMap<>();
    private Map<String, Object> userContext = new HashMap<>();
    private Map<String, Object> executionContext = new HashMap<>();
    private Map<String, Object> environmentContext = new HashMap<>();
    private Map<String, Object> traceInformation = new HashMap<>();

    public Map<String, Object> getPromptContext() { return promptContext; }
    public void setPromptContext(Map<String, Object> promptContext) { this.promptContext = promptContext; }
    
    public Map<String, Object> getWorkflowContext() { return workflowContext; }
    public void setWorkflowContext(Map<String, Object> workflowContext) { this.workflowContext = workflowContext; }
    
    public Map<String, Object> getMemoryContext() { return memoryContext; }
    public void setMemoryContext(Map<String, Object> memoryContext) { this.memoryContext = memoryContext; }
    
    public Map<String, Object> getUserContext() { return userContext; }
    public void setUserContext(Map<String, Object> userContext) { this.userContext = userContext; }
    
    public Map<String, Object> getExecutionContext() { return executionContext; }
    public void setExecutionContext(Map<String, Object> executionContext) { this.executionContext = executionContext; }
    
    public Map<String, Object> getEnvironmentContext() { return environmentContext; }
    public void setEnvironmentContext(Map<String, Object> environmentContext) { this.environmentContext = environmentContext; }
    
    public Map<String, Object> getTraceInformation() { return traceInformation; }
    public void setTraceInformation(Map<String, Object> traceInformation) { this.traceInformation = traceInformation; }
}