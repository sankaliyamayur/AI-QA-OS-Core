package com.aiqaos.core.event;

public class SecurityEvent extends BaseEvent {
    private String accessedPrincipal;
    private String attemptedAction;

    public String getAccessedPrincipal() { return accessedPrincipal; }
    public void setAccessedPrincipal(String accessedPrincipal) { this.accessedPrincipal = accessedPrincipal; }
    public String getAttemptedAction() { return attemptedAction; }
    public void setAttemptedAction(String attemptedAction) { this.attemptedAction = attemptedAction; }
}