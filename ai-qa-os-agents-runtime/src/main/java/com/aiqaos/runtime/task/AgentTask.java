package com.aiqaos.runtime.task;

import java.util.UUID;

public class AgentTask {
    private UUID taskId;
    private String goal;

    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
}