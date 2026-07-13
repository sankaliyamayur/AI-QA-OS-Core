package com.aiqaos.security.tool;

import com.aiqaos.security.agent.AgentPermissionManager;
import com.aiqaos.security.audit.AuditLogger;
import com.aiqaos.security.context.SecurityContext;
import com.aiqaos.security.event.SecurityEvent;
import com.aiqaos.security.event.SecurityEventPublisher;
import com.aiqaos.security.event.SecurityEventType;
import com.aiqaos.security.policy.DefaultPolicyEngine;
import com.aiqaos.security.policy.PolicyRequest;
import org.springframework.stereotype.Component;

@Component
public class ToolSecurityGateway {

    private final AgentPermissionManager agentPermissionManager;
    private final DefaultPolicyEngine policyEngine;
    private final SecurityEventPublisher eventPublisher;
    private final AuditLogger auditLogger;

    public ToolSecurityGateway(AgentPermissionManager agentPermissionManager,
                                DefaultPolicyEngine policyEngine,
                                SecurityEventPublisher eventPublisher,
                                AuditLogger auditLogger) {
        this.agentPermissionManager = agentPermissionManager;
        this.policyEngine = policyEngine;
        this.eventPublisher = eventPublisher;
        this.auditLogger = auditLogger;
    }

    public boolean authorize(SecurityContext context, String agentType, String toolAction) {
        // 1. Check agent-level permission
        if (!agentPermissionManager.isAllowed(agentType, toolAction)) {
            publishDenied(context, toolAction);
            return false;
        }

        // 2. Check policy engine
        PolicyRequest req = new PolicyRequest();
        req.setAction(toolAction);
        req.setEnvironment(context.getEnvironment());
        if (!policyEngine.evaluate(context, req)) {
            publishDenied(context, toolAction);
            return false;
        }

        // 3. Allow
        SecurityEvent event = new SecurityEvent();
        event.setType(SecurityEventType.TOOL_ALLOWED);
        event.setAgentId(agentType);
        event.setAction(toolAction);
        event.setResult("ALLOWED");
        eventPublisher.publish(event);
        return true;
    }

    private void publishDenied(SecurityContext context, String toolAction) {
        SecurityEvent event = new SecurityEvent();
        event.setType(SecurityEventType.TOOL_DENIED);
        event.setAgentId(context != null ? context.getAgentId() : "UNKNOWN");
        event.setAction(toolAction);
        event.setResult("DENIED");
        eventPublisher.publish(event);
    }
}