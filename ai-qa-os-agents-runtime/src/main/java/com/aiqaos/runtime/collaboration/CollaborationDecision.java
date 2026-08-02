package com.aiqaos.runtime.collaboration;

/**
 * AGT-2 (ADR-083): the mediator's verdict on a collaboration request — allowed, or denied with a
 * specific reason.
 */
public record CollaborationDecision(boolean allowed, String reason) {

    public static CollaborationDecision allow() {
        return new CollaborationDecision(true, "allowed");
    }

    public static CollaborationDecision deny(String reason) {
        return new CollaborationDecision(false, reason);
    }
}
