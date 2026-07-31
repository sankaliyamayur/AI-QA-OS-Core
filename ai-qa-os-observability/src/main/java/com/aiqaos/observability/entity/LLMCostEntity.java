package com.aiqaos.observability.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "observability_llm_costs")
public class LLMCostEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-E: tenant discriminator (ADR-054/056) — per-tenant LLM cost isolation.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    private String requestId;
    private String agentType;
    private String purpose;
    private long latencyMs;
    private String provider;
    private String model;
    private long inputTokens;
    private long outputTokens;
    private double cost;
    private LocalDateTime timestamp;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public long getInputTokens() { return inputTokens; }
    public void setInputTokens(long inputTokens) { this.inputTokens = inputTokens; }
    public long getOutputTokens() { return outputTokens; }
    public void setOutputTokens(long outputTokens) { this.outputTokens = outputTokens; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}