package com.aiqaos.reporting.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "failure_analyses")
public class FailureAnalysisEntity extends BaseEntity {

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "test_case", nullable = false)
    private String testCase;

    @Column(name = "root_cause")
    private String rootCause;

    @Column(name = "recommendation")
    private String recommendation;

    @Column(name = "confidence")
    private double confidence;

    @Column(name = "ai_generated")
    private boolean aiGenerated;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getTestCase() { return testCase; }
    public void setTestCase(String testCase) { this.testCase = testCase; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
}