package com.aiqaos.gateway.dto;

import java.time.LocalDateTime;

public class GatewayResponseDTO {
    private String status;
    private String correlationId;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Object data;

    public static GatewayResponseDTO success(String correlationId, Object data) {
        GatewayResponseDTO r = new GatewayResponseDTO();
        r.status = "SUCCESS"; r.correlationId = correlationId; r.data = data;
        return r;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}