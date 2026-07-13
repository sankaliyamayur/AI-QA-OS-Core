package com.aiqaos.agent.component;

import com.aiqaos.core.enums.AgentType;
import com.aiqaos.agent.tool.AgentTool;
import com.aiqaos.core.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class AgentSecurityFilter {
    public void checkPermission(AgentTool tool, AgentType agentType) {
        if (!tool.isAllowedFor(agentType)) {
            throw new ValidationException(
                String.format("Security Violation: Agent type %s is not permitted to execute tool %s", 
                agentType, tool.getName())
            );
        }
    }
}