package com.aiqaos.brain.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "brain_reasoning_traces")
public class ReasoningTraceEntity extends BaseEntity {

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "step_number")
    private int stepNumber;

    @Column(name = "step_description")
    private String stepDescription;

    @Column(name = "thought_process", length = 1000)
    private String thoughtProcess;

    @Column(name = "action_taken")
    private String actionTaken;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public UUID getDecisionId() { return decisionId; }
    public void setDecisionId(UUID decisionId) { this.decisionId = decisionId; }
    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }
    public String getStepDescription() { return stepDescription; }
    public void setStepDescription(String stepDescription) { this.stepDescription = stepDescription; }
    public String getThoughtProcess() { return thoughtProcess; }
    public void setThoughtProcess(String thoughtProcess) { this.thoughtProcess = thoughtProcess; }
    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}