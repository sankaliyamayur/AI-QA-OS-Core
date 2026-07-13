package com.aiqaos.agent.tool;

import com.aiqaos.core.enums.AgentType;

public interface AgentTool {
    String getName();
    String getDescription();
    boolean isAllowedFor(AgentType agentType);
    String execute(String input);
}