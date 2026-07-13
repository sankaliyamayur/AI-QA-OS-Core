package com.aiqaos.security.event;

import java.time.LocalDateTime;

public class SecurityEvent {

    private SecurityEventType type;
    private String userId;
    private String agentId;
    private String workflowId;
    private String action;
    private String result;
    private String ipAddress;
    private LocalDateTime timestamp;

    public SecurityEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public SecurityEventType getType() { return type; }
    public void setType(SecurityEventType type) { this.type = type; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}