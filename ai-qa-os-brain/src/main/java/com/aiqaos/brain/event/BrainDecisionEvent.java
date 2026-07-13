package com.aiqaos.brain.event;

import java.util.UUID;

public class BrainDecisionEvent {
    private final UUID decisionId;

    public BrainDecisionEvent(UUID decisionId) {
        this.decisionId = decisionId;
    }

    public UUID getDecisionId() { return decisionId; }
}