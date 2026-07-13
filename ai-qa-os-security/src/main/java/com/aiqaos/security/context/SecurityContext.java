package com.aiqaos.security.context;

import java.time.LocalDateTime;
import java.util.List;

public class SecurityContext {

    private String userId;
    private List<String> roles;
    private List<String> permissions;
    private String agentId;
    private String workflowId;
    private String environment;
    private LocalDateTime authenticatedAt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public LocalDateTime getAuthenticatedAt() { return authenticatedAt; }
    public void setAuthenticatedAt(LocalDateTime authenticatedAt) { this.authenticatedAt = authenticatedAt; }
}