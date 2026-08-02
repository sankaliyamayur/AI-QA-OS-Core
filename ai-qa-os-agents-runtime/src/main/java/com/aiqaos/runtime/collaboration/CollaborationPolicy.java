package com.aiqaos.runtime.collaboration;

import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * AGT-2 (ADR-083): the collaboration mediator's rules. Certain capabilities are <b>privileged</b> —
 * a requester may only have the target perform them if the requester itself holds the granting
 * {@code supervisor} capability. This prevents cross-agent privilege escalation in a multi-agent org.
 */
@Component
public class CollaborationPolicy {

    /** Holding this capability authorizes a requester to invoke privileged capabilities on a target. */
    public static final String SUPERVISOR_CAPABILITY = "supervisor";

    private final Set<String> privilegedCapabilities = Set.of("execute.privileged", "modify.governance");

    public boolean isPrivileged(String capability) {
        return capability != null && privilegedCapabilities.contains(capability);
    }

    public String supervisorCapability() {
        return SUPERVISOR_CAPABILITY;
    }
}
