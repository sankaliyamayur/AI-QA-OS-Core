package com.aiqaos.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FailurePattern implements Serializable {
    private static final long serialVersionUID = 1L;

    private String patternId;
    private String errorType;
    private String rootCause;
    private String impactedComponent;
    private int occurrenceCount;
    private double confidence;
    private LocalDateTime lastDetected;

    public FailurePattern() {
        this.lastDetected = LocalDateTime.now();
    }

    public String getPatternId() { return patternId; }
    public void setPatternId(String patternId) { this.patternId = patternId; }

    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getImpactedComponent() { return impactedComponent; }
    public void setImpactedComponent(String impactedComponent) { this.impactedComponent = impactedComponent; }

    public int getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public LocalDateTime getLastDetected() { return lastDetected; }
    public void setLastDetected(LocalDateTime lastDetected) { this.lastDetected = lastDetected; }
}
