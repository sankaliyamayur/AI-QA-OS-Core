package com.aiqaos.agent.context;

import com.aiqaos.core.context.AgentContext;
import java.util.HashMap;
import java.util.Map;

public class AgentContextImpl extends AgentContext {
    private String projectId;
    private String workflowId;
    private String userId;
    private String environment;
    private Map<String, Object> memoryContext = new HashMap<>();
    private Map<String, Object> executionContext = new HashMap<>();

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public Map<String, Object> getMemoryContext() { return memoryContext; }
    public void setMemoryContext(Map<String, Object> memoryContext) { this.memoryContext = memoryContext; }

    public Map<String, Object> getExecutionContext() { return executionContext; }
    public void setExecutionContext(Map<String, Object> executionContext) { this.executionContext = executionContext; }
}