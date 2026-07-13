package com.aiqaos.agent.tool;

import com.aiqaos.core.enums.AgentType;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTool implements AgentTool {
    @Override
    public String getName() { return "DatabaseTool"; }
    @Override
    public String getDescription() { return "Fetches database configurations and metadata schema."; }
    @Override
    public boolean isAllowedFor(AgentType agentType) {
        return agentType == AgentType.QA_ENGINEER;
    }
    @Override
    public String execute(String input) {
        if (input != null && (input.toUpperCase().contains("DELETE") || input.toUpperCase().contains("DROP"))) {
            return "Execution Rejected: Mutation operations are blocked.";
        }
        return "DB Schema successfully mapped.";
    }
}