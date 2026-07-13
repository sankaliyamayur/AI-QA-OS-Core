package com.aiqaos.gateway.event;

import java.time.LocalDateTime;

public class GatewayEvent {
    private GatewayEventType type;
    private String correlationId;
    private String userId;
    private String endpoint;
    private long durationMs;
    private int statusCode;
    private LocalDateTime timestamp;

    public GatewayEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public GatewayEventType getType() { return type; }
    public void setType(GatewayEventType type) { this.type = type; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}