package com.aiqaos.runtime.collaboration;

import java.util.Set;

/**
 * AGT-2 (ADR-083): one agent's request to collaborate with (delegate a capability to) another. Carries
 * the requester's and target's advertised capabilities so the {@link CollaborationMediator} can decide
 * statelessly.
 *
 * @param requesterId          the agent asking to collaborate
 * @param requesterCapabilities the requester's own capabilities (for privilege checks)
 * @param targetId             the agent being asked
 * @param targetCapabilities   the target's advertised capabilities
 * @param requestedCapability  the capability the requester wants the target to perform
 */
public record CollaborationRequest(
        String requesterId,
        Set<String> requesterCapabilities,
        String targetId,
        Set<String> targetCapabilities,
        String requestedCapability) {

    public CollaborationRequest {
        requesterCapabilities = requesterCapabilities != null ? Set.copyOf(requesterCapabilities) : Set.of();
        targetCapabilities = targetCapabilities != null ? Set.copyOf(targetCapabilities) : Set.of();
    }
}
