package com.aiqaos.workflow.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TimelineEventDTO {
    private UUID id;
    private UUID executionId;
    private int sequenceNumber;
    private String eventType;
    private String stepName;
    private String description;
    private String status;
    private Long durationMs;
    private LocalDateTime occurredAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
