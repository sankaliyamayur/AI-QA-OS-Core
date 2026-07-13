package com.aiqaos.brain.model;

import com.aiqaos.core.dto.BaseDTO;

public class BrainResponseDTO implements BaseDTO {
    private String decision;
    private double confidence;

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}