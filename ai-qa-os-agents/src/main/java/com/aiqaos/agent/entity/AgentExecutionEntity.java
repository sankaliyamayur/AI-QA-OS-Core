package com.aiqaos.agent.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "agent_executions")
public class AgentExecutionEntity extends BaseEntity {

    @Column(name = "agent_id", nullable = false)
    private UUID agentId;

    @Lob
    @Column(name = "request_payload", nullable = false)
    private String request;

    @Lob
    @Column(name = "response_payload", nullable = false)
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