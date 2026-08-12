package com.aiqaos.observability.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "observability_events")
public class EventEntity extends BaseEntity {
    private String eventType;
    private String source;

    // V27: TEXT, not @Lob. Hibernate's PostgreSQL dialect maps @Lob String to a large object (oid),
    // which outlives its row — every event leaked one on delete. Nothing reads payload today, so it
    // never hit the auto-commit failure that took down agent_traces (V26), but it would the moment a
    // read was added. Same reasoning as PromptExecutionEntity.finalCompiledPrompt (V23).
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
    private LocalDateTime createdAt;

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}