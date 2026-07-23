package com.aiqaos.eval.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Durable record of a single evaluator's result, so eval outcomes survive for PE-3's quality
 * dashboard and GOV-1's audit. Extends {@link BaseEntity} for the standard audit columns.
 */
@Entity
@Table(name = "eval_results")
public class EvaluationResultEntity extends BaseEntity {

    @Column(name = "result_id", nullable = false)
    private UUID resultId;

    @Column(name = "suite")
    private String suite;

    @Column(name = "case_id")
    private String caseId;

    @Column(name = "evaluator")
    private String evaluator;

    @Column(name = "score")
    private double score;

    @Column(name = "passed")
    private boolean passed;

    @Column(name = "prompt_version")
    private String promptVersion;

    @Column(name = "agent_type")
    private String agentType;

    @Column(name = "reason", length = 2000)
    private String reason;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    public UUID getResultId() {
        return resultId;
    }

    public void setResultId(UUID resultId) {
        this.resultId = resultId;
    }

    public String getSuite() {
        return suite;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(String evaluator) {
        this.evaluator = evaluator;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
