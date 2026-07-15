package com.aiqaos.dashboard.dto;

import com.aiqaos.observability.entity.AgentMetricEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public class AgentMetricDTO {
    private UUID id;
    private UUID executionId;
    private UUID workflowId;
    private String agentType;
    private String operation;
    private String correlationId;
    private long durationMs;
    private Long tokensUsed;
    private Double cost;
    private boolean success;
    private String errorMessage;
    private LocalDateTime recordedAt;

    public static AgentMetricDTO from(AgentMetricEntity entity) {
        AgentMetricDTO dto = new AgentMetricDTO();
        dto.id = entity.getId();
        dto.executionId = entity.getExecutionId();
        dto.workflowId = entity.getWorkflowId();
        dto.agentType = entity.getAgentType();
        dto.operation = entity.getOperation();
        dto.correlationId = entity.getCorrelationId();
        dto.durationMs = entity.getDurationMs();
        dto.tokensUsed = entity.getTokensUsed();
        dto.cost = entity.getCost();
        dto.success = entity.isSuccess();
        dto.errorMessage = entity.getErrorMessage();
        dto.recordedAt = entity.getRecordedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getExecutionId() { return executionId; }
    public UUID getWorkflowId() { return workflowId; }
    public String getAgentType() { return agentType; }
    public String getOperation() { return operation; }
    public String getCorrelationId() { return correlationId; }
    public long getDurationMs() { return durationMs; }
    public Long getTokensUsed() { return tokensUsed; }
    public Double getCost() { return cost; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
