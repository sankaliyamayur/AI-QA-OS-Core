package com.aiqaos.observability.model;

public class TraceDTO {
    private String correlationId;
    private String service;
    private long duration;

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
}