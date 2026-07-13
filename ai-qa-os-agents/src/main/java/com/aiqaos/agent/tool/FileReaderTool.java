package com.aiqaos.agent.tool;

import com.aiqaos.core.enums.AgentType;
import org.springframework.stereotype.Component;

@Component
public class FileReaderTool implements AgentTool {
    @Override
    public String getName() { return "FileReaderTool"; }
    @Override
    public String getDescription() { return "Reads content from dynamic configuration files."; }
    @Override
    public boolean isAllowedFor(AgentType agentType) {
        return agentType == AgentType.QA_ENGINEER || agentType == AgentType.SUPERVISOR;
    }
    @Override
    public String execute(String input) {
        return "Loaded text template lines from repository target: " + input;
    }
}