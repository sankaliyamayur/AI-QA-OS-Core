package com.aiqaos.agent.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.UUID;

public class AgentRequestDTO implements BaseDTO {
    private UUID requestId;
    private String agentType;
    private String task;
    private String inputData;
    private String context;
    private boolean memoryRequired;
    private String priority;

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }
    public String getInputData() { return inputData; }
    public void setInputData(String inputData) { this.inputData = inputData; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public boolean isMemoryRequired() { return memoryRequired; }
    public void setMemoryRequired(boolean memoryRequired) { this.memoryRequired = memoryRequired; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}