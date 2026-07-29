package com.aiqaos.core.contract;

/**
 * LRN-4: a learned improvement proposed for adoption, reduced to what the {@link SafeAdoptionGate}
 * needs to decide — its measured quality ({@code evalScore}, from MOD-3/PE-1) and its trust
 * ({@code confidence}, evaluated by the AI-1 {@link ConfidenceGate}). This is the core-level view of
 * an LRN-1 {@code ImprovementProposal}; the mapping is wired at adoption time (FI-LRN4-A).
 */
public class AdoptionCandidate {

    private final String candidateId;
    private final AdoptionKind kind;
    private final double evalScore;
    private final double confidence;
    private final String description;
    private final String correlationId;

    public AdoptionCandidate(String candidateId, AdoptionKind kind, double evalScore,
                             double confidence, String description, String correlationId) {
        this.candidateId = candidateId;
        this.kind = kind;
        this.evalScore = evalScore;
        this.confidence = confidence;
        this.description = description;
        this.correlationId = correlationId;
    }

    public String getCandidateId() { return candidateId; }
    public AdoptionKind getKind() { return kind; }
    public double getEvalScore() { return evalScore; }
    public double getConfidence() { return confidence; }
    public String getDescription() { return description; }
    public String getCorrelationId() { return correlationId; }
}
