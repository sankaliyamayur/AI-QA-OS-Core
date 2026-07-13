package com.aiqaos.agent.tool;

import com.aiqaos.core.enums.AgentType;
import org.springframework.stereotype.Component;

@Component
public class CodeGeneratorTool implements AgentTool {
    @Override
    public String getName() { return "CodeGeneratorTool"; }
    @Override
    public String getDescription() { return "Builds scaffolding structure automation classes."; }
    @Override
    public boolean isAllowedFor(AgentType agentType) {
        return agentType == AgentType.CODER;
    }
    @Override
    public String execute(String input) {
        return "Scaffolded test elements payload classes: " + input;
    }
}