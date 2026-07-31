package com.aiqaos.orchestration.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI-2 — durable record of a human-review event (the review queue).
 * Written PENDING when a run pauses (AI-1 HUMAN_REVIEW); updated APPROVED/REJECTED on decision.
 * Persisted to the shared DB so the dashboard (a different JVM) can display the queue.
 */
@Entity
@Table(name = "human_reviews")
public class HumanReviewEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C: tenant discriminator — Hibernate stamps on insert, filters on read (ADR-054).
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "execution_id")
    private UUID executionId;

    @Column(name = "step_name")
    private String stepName;

    @Column(name = "confidence")
    private double confidence;

    @Column(name = "status", nullable = false)
    private String status; // PENDING | APPROVED | REJECTED

    @Column(name = "reviewer")
    private String reviewer;

    @Column(name = "decision_comment", length = 1000)
    private String decisionComment;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "decided_time")
    private LocalDateTime decidedTime;

    public UUID getReviewId() { return reviewId; }
    public void setReviewId(UUID reviewId) { this.reviewId = reviewId; }
    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public String getDecisionComment() { return decisionComment; }
    public void setDecisionComment(String decisionComment) { this.decisionComment = decisionComment; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getDecidedTime() { return decidedTime; }
    public void setDecidedTime(LocalDateTime decidedTime) { this.decidedTime = decidedTime; }
}
