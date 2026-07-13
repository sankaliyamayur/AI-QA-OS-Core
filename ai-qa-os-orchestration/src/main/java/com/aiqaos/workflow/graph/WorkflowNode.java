package com.aiqaos.workflow.graph;

public class WorkflowNode {
    private String stepId;
    private String agentType;
    private int executionOrder;
    private int retryCount;
    private long timeoutSeconds;
    private String condition;
    private boolean parallel;
    private String pluginType;
    private String pluginInput;

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public int getExecutionOrder() { return executionOrder; }
    public void setExecutionOrder(int executionOrder) { this.executionOrder = executionOrder; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public long getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(long timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public boolean isParallel() { return parallel; }
    public void setParallel(boolean parallel) { this.parallel = parallel; }
    public String getPluginType() { return pluginType; }
    public void setPluginType(String pluginType) { this.pluginType = pluginType; }
    public String getPluginInput() { return pluginInput; }
    public void setPluginInput(String pluginInput) { this.pluginInput = pluginInput; }
}