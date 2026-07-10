package com.aiqaos.core.model;

import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class AgentTask {
    private String taskId = UUID.randomUUID().toString();
    private String workflowId;
    private String agentCapability;
    private String instruction;
    private Map<String, Object> contextParameters;
    private TaskStatus status = TaskStatus.PENDING;
}
