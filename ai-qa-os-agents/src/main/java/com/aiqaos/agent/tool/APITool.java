package com.aiqaos.agent.tool;

import com.aiqaos.core.enums.AgentType;
import org.springframework.stereotype.Component;

@Component
public class APITool implements AgentTool {
    @Override
    public String getName() { return "APITool"; }
    @Override
    public String getDescription() { return "Performs REST operations against mock Swagger frameworks."; }
    @Override
    public boolean isAllowedFor(AgentType agentType) {
        return agentType == AgentType.QA_ENGINEER || agentType == AgentType.SUPERVISOR;
    }
    @Override
    public String execute(String input) {
        return "REST payload output: " + input;
    }
}