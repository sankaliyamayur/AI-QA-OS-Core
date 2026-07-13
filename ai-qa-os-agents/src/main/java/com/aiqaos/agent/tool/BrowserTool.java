package com.aiqaos.agent.tool;

import com.aiqaos.core.enums.AgentType;
import org.springframework.stereotype.Component;

@Component
public class BrowserTool implements AgentTool {
    @Override
    public String getName() { return "BrowserTool"; }
    @Override
    public String getDescription() { return "Launches isolated browser driver execution tests."; }
    @Override
    public boolean isAllowedFor(AgentType agentType) {
        return agentType == AgentType.QA_ENGINEER || agentType == AgentType.VALIDATOR;
    }
    @Override
    public String execute(String input) {
        return "Executed web action driver instructions: " + input;
    }
}