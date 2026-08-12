package com.aiqaos.agent.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "agent_executions")
public class AgentExecutionEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C ext: tenant discriminator (ADR-054/057) — Hibernate stamps on insert, filters on read.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "agent_id", nullable = false)
    private UUID agentId;

    // V27: TEXT, not @Lob — see V26/agent_traces. Dormant (no callers, table empty); converted so
    // the first component to use this repository does not inherit the leak and the read failure.
    @Column(name = "request_payload", nullable = false, columnDefinition = "TEXT")
    private String request;

    @Column(name = "response_payload", nullable = false, columnDefinition = "TEXT")
    private String response;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "execution_time_ms")
    private long executionTime;

    public UUID getAgentId() { return agentId; }
    public void setAgentId(UUID agentId) { this.agentId = agentId; }

    public String getRequest() { return request; }
    public void setRequest(String request) { this.request = request; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
}