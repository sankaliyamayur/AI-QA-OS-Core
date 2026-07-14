package com.aiqaos.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LearningEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String executionId;
    private String eventType; // FAILURE_PATTERN, SUCCESS_PATTERN, SCRIPT_IMPROVEMENT, PROMPT_IMPROVEMENT
    private String category;
    private String description;
    private String sourceAgent;
    private LocalDateTime createdTime;

    public LearningEvent() {
        this.createdTime = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSourceAgent() { return sourceAgent; }
    public void setSourceAgent(String sourceAgent) { this.sourceAgent = sourceAgent; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
