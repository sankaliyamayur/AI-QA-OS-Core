package com.aiqaos.observability.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_traces")
public class AgentTraceEntity extends BaseEntity {
    private String correlationId;
    private String agentType;
    private String purpose;
    private String provider;
    private String model;

    // V26: TEXT, not @Lob. Hibernate's PostgreSQL dialect maps @Lob String to a large object (oid),
    // and a large object can only be read inside a transaction — so every read of these two columns
    // failed with "Large Objects may not be used in auto-commit mode" while writes succeeded. They
    // also outlive their row, leaking one object per column on delete. Same reasoning as
    // PromptExecutionEntity.finalCompiledPrompt (V23), which is already TEXT.
    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    private Long promptTokens;
    private Long completionTokens;
    private Long latencyMs;
    private LocalDateTime timestamp;

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public Long getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Long promptTokens) { this.promptTokens = promptTokens; }
    public Long getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Long completionTokens) { this.completionTokens = completionTokens; }
    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
