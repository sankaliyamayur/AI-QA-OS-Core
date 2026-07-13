package com.aiqaos.runtime.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_runtimes")
public class AgentRuntimeEntity extends BaseEntity {

    @Column(name = "runtime_id", nullable = false)
    private UUID runtimeId;

    @Column(name = "agent_type", nullable = false)
    private String agentType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "current_task")
    private String currentTask;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "stopped_at")
    private LocalDateTime stoppedAt;

    public UUID getRuntimeId() { return runtimeId; }
    public void setRuntimeId(UUID runtimeId) { this.runtimeId = runtimeId; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentTask() { return currentTask; }
    public void setCurrentTask(String currentTask) { this.currentTask = currentTask; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getStoppedAt() { return stoppedAt; }
    public void setStoppedAt(LocalDateTime stoppedAt) { this.stoppedAt = stoppedAt; }
}