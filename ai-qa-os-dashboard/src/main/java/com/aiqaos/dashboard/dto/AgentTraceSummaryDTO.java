package com.aiqaos.dashboard.dto;

import com.aiqaos.observability.entity.AgentTraceEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/** List-view projection of an agent trace - omits full prompt/response bodies. */
public class AgentTraceSummaryDTO {
    private static final int PREVIEW_LENGTH = 200;

    private UUID id;
    private String correlationId;
    private String agentType;
    private String purpose;
    private String provider;
    private String model;
    private String promptPreview;
    private String responsePreview;
    private Long promptTokens;
    private Long completionTokens;
    private Long latencyMs;
    private LocalDateTime timestamp;

    public static AgentTraceSummaryDTO from(AgentTraceEntity entity) {
        AgentTraceSummaryDTO dto = new AgentTraceSummaryDTO();
        dto.id = entity.getId();
        dto.correlationId = entity.getCorrelationId();
        dto.agentType = entity.getAgentType();
        dto.purpose = entity.getPurpose();
        dto.provider = entity.getProvider();
        dto.model = entity.getModel();
        dto.promptPreview = truncate(entity.getPrompt());
        dto.responsePreview = truncate(entity.getResponse());
        dto.promptTokens = entity.getPromptTokens();
        dto.completionTokens = entity.getCompletionTokens();
        dto.latencyMs = entity.getLatencyMs();
        dto.timestamp = entity.getTimestamp();
        return dto;
    }

    private static String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > PREVIEW_LENGTH ? text.substring(0, PREVIEW_LENGTH) + "..." : text;
    }

    public UUID getId() { return id; }
    public String getCorrelationId() { return correlationId; }
    public String getAgentType() { return agentType; }
    public String getPurpose() { return purpose; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public String getPromptPreview() { return promptPreview; }
    public String getResponsePreview() { return responsePreview; }
    public Long getPromptTokens() { return promptTokens; }
    public Long getCompletionTokens() { return completionTokens; }
    public Long getLatencyMs() { return latencyMs; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
