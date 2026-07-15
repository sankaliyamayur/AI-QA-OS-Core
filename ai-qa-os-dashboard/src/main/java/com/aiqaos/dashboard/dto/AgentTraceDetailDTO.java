package com.aiqaos.dashboard.dto;

import com.aiqaos.observability.entity.AgentTraceEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/** Full prompt/response detail view for a single agent trace. */
public class AgentTraceDetailDTO {
    private UUID id;
    private String correlationId;
    private String agentType;
    private String purpose;
    private String provider;
    private String model;
    private String prompt;
    private String response;
    private Long promptTokens;
    private Long completionTokens;
    private Long latencyMs;
    private LocalDateTime timestamp;

    public static AgentTraceDetailDTO from(AgentTraceEntity entity) {
        AgentTraceDetailDTO dto = new AgentTraceDetailDTO();
        dto.id = entity.getId();
        dto.correlationId = entity.getCorrelationId();
        dto.agentType = entity.getAgentType();
        dto.purpose = entity.getPurpose();
        dto.provider = entity.getProvider();
        dto.model = entity.getModel();
        dto.prompt = entity.getPrompt();
        dto.response = entity.getResponse();
        dto.promptTokens = entity.getPromptTokens();
        dto.completionTokens = entity.getCompletionTokens();
        dto.latencyMs = entity.getLatencyMs();
        dto.timestamp = entity.getTimestamp();
        return dto;
    }

    public UUID getId() { return id; }
    public String getCorrelationId() { return correlationId; }
    public String getAgentType() { return agentType; }
    public String getPurpose() { return purpose; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public String getPrompt() { return prompt; }
    public String getResponse() { return response; }
    public Long getPromptTokens() { return promptTokens; }
    public Long getCompletionTokens() { return completionTokens; }
    public Long getLatencyMs() { return latencyMs; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
