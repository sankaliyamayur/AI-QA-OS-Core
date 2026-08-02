package com.aiqaos.runtime.collaboration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** AGT-2 (ADR-083): the collaboration mediator's rules — governed cross-agent delegation. */
class CollaborationMediatorTest {

    private final CollaborationMediator mediator = new CollaborationMediator(new CollaborationPolicy());

    private static CollaborationRequest req(String requester, Set<String> reqCaps,
                                            String target, Set<String> targetCaps, String capability) {
        return new CollaborationRequest(requester, reqCaps, target, targetCaps, capability);
    }

    @Test
    void allowsWhenTargetProvidesNonPrivilegedCapability() {
        assertTrue(mediator.mediate(
                req("a", Set.of(), "b", Set.of("report.export"), "report.export")).allowed());
    }

    @Test
    void deniesSelfCollaboration() {
        CollaborationDecision d = mediator.mediate(req("a", Set.of(), "a", Set.of("x"), "x"));
        assertFalse(d.allowed());
        assertTrue(d.reason().contains("self-collaboration"));
    }

    @Test
    void deniesWhenTargetLacksCapability() {
        CollaborationDecision d = mediator.mediate(req("a", Set.of(), "b", Set.of("y"), "x"));
        assertFalse(d.allowed());
        assertTrue(d.reason().contains("does not provide"));
    }

    @Test
    void deniesPrivilegedCapabilityWithoutSupervisor() {
        CollaborationDecision d = mediator.mediate(
                req("a", Set.of(), "b", Set.of("execute.privileged"), "execute.privileged"));
        assertFalse(d.allowed());
        assertTrue(d.reason().contains("supervisor"));
    }

    @Test
    void allowsPrivilegedCapabilityWithSupervisor() {
        assertTrue(mediator.mediate(
                req("a", Set.of("supervisor"), "b", Set.of("execute.privileged"), "execute.privileged")).allowed());
    }

    @Test
    void deniesBlankOrNullRequest() {
        assertFalse(mediator.mediate(req("", Set.of(), "b", Set.of("x"), "x")).allowed());
        assertFalse(mediator.mediate(null).allowed());
    }
}
