package com.aiqaos.core.event;

/**
 * SCALE-2 (ADR-064): the on-the-wire wrapper for a {@link BaseEvent} crossing the Kafka boundary.
 * {@code type} is the concrete class FQN so the consumer reconstructs the exact subtype; {@code payload}
 * is the event serialised as JSON. Non-invasive — avoids polymorphic type info on {@code BaseEvent} itself.
 */
public class EventEnvelope {

    private String type;
    private String payload;

    public EventEnvelope() {
    }

    public EventEnvelope(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
