package com.aiqaos.agent.roster;

/**
 * AGT-1: one entry in the agent roster — the agent's roster {@code name}, its {@link AgentCategory},
 * its {@link AgentStatus}, and (when implemented) the {@code implementingClass} that realises it.
 */
public final class AgentDescriptor {

    private final String name;
    private final AgentCategory category;
    private final AgentStatus status;
    private final String implementingClass; // nullable for DESIGNED/FUTURE

    public AgentDescriptor(String name, AgentCategory category, AgentStatus status,
                           String implementingClass) {
        this.name = name;
        this.category = category;
        this.status = status;
        this.implementingClass = implementingClass;
    }

    public String getName() { return name; }
    public AgentCategory getCategory() { return category; }
    public AgentStatus getStatus() { return status; }
    public String getImplementingClass() { return implementingClass; }

    public boolean isImplemented() {
        return status == AgentStatus.IMPLEMENTED;
    }

    @Override
    public String toString() {
        return "AgentDescriptor{" + name + " [" + status + "] " + category.getDisplayName()
                + (implementingClass != null ? " (" + implementingClass + ")" : "") + "}";
    }
}
