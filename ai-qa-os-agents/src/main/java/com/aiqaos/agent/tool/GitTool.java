package com.aiqaos.agent.tool;

import com.aiqaos.core.enums.AgentType;
import org.springframework.stereotype.Component;

@Component
public class GitTool implements AgentTool {
    @Override
    public String getName() { return "GitTool"; }
    @Override
    public String getDescription() { return "Fetches file diffs or updates code repository commits."; }
    @Override
    public boolean isAllowedFor(AgentType agentType) {
        return agentType == AgentType.CODER;
    }
    @Override
    public String execute(String input) {
        return "Created test script patch commit: " + input;
    }
}