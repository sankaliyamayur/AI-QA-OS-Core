package com.aiqaos.runtime.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "agent_runtime_tasks")
public class AgentTaskEntity extends BaseEntity {

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "agent_id", nullable = false)
    private UUID agentId;

    @Column(name = "goal", nullable = false, length = 1000)
    private String goal;

    @Column(name = "input_context", length = 1000)
    private String inputContext;

    @Column(name = "result", length = 1000)
    private String result;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "execution_time")
    private long executionTime;

    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    public UUID getAgentId() { return agentId; }
    public void setAgentId(UUID agentId) { this.agentId = agentId; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getInputContext() { return inputContext; }
    public void setInputContext(String inputContext) { this.inputContext = inputContext; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
}