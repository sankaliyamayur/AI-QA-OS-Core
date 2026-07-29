package com.aiqaos.integration.plugin.integration;

/**
 * PLG-2: a request to an integration plugin — an {@code action} (e.g. {@code commit}, {@code issue},
 * {@code notify}, {@code trigger}) and its {@code payload}.
 */
public final class IntegrationRequest {

    private final String action;
    private final String payload;
    private final String correlationId;

    public IntegrationRequest(String action, String payload, String correlationId) {
        this.action = action;
        this.payload = payload;
        this.correlationId = correlationId;
    }

    public static IntegrationRequest of(String action, String payload) {
        return new IntegrationRequest(action, payload, null);
    }

    public String getAction() { return action; }
    public String getPayload() { return payload; }
    public String getCorrelationId() { return correlationId; }
}
