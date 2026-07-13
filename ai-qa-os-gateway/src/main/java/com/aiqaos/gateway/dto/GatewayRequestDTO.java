package com.aiqaos.gateway.dto;

import java.time.LocalDateTime;

public class GatewayRequestDTO {
    private String correlationId;
    private String userId;
    private LocalDateTime timestamp = LocalDateTime.now();

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}