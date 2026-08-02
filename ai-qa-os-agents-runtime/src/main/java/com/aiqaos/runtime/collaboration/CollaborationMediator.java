package com.aiqaos.runtime.collaboration;

import org.springframework.stereotype.Component;

/**
 * AGT-2 (ADR-083): the multi-agent collaboration mediator — governs whether one agent may collaborate
 * with (delegate a capability to) another, "under mediator rules". A stateless, ordered decision:
 * well-formed → not self → the target actually provides the capability → privilege gate → allow.
 * In the multi-agent org, cross-agent delegation over {@code AgentMessageBus} consults this mediator
 * before routing (FI-AGT2-A).
 */
@Component
public class CollaborationMediator {

    private final CollaborationPolicy policy;

    public CollaborationMediator(CollaborationPolicy policy) {
        this.policy = policy;
    }

    public CollaborationDecision mediate(CollaborationRequest request) {
        if (request == null) {
            return CollaborationDecision.deny("request is required");
        }
        if (isBlank(request.requesterId()) || isBlank(request.targetId()) || isBlank(request.requestedCapability())) {
            return CollaborationDecision.deny("requesterId, targetId and requestedCapability are required");
        }
        if (request.requesterId().equals(request.targetId())) {
            return CollaborationDecision.deny("no self-collaboration");
        }
        if (!request.targetCapabilities().contains(request.requestedCapability())) {
            return CollaborationDecision.deny("target '" + request.targetId()
                    + "' does not provide capability '" + request.requestedCapability() + "'");
        }
        if (policy.isPrivileged(request.requestedCapability())
                && !request.requesterCapabilities().contains(policy.supervisorCapability())) {
            return CollaborationDecision.deny("privileged capability '" + request.requestedCapability()
                    + "' requires the '" + policy.supervisorCapability() + "' capability");
        }
        return CollaborationDecision.allow();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
