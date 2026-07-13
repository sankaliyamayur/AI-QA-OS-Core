package com.aiqaos.brain.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "brain_decisions")
public class DecisionEntity extends BaseEntity {

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "user_input", nullable = false, length = 1000)
    private String userInput;

    @Column(name = "decision", nullable = false)
    private String decision;

    @Column(name = "confidence")
    private double confidence;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public UUID getDecisionId() { return decisionId; }
    public void setDecisionId(UUID decisionId) { this.decisionId = decisionId; }
    public String getUserInput() { return userInput; }
    public void setUserInput(String userInput) { this.userInput = userInput; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}