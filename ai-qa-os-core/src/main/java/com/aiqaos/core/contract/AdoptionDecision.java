package com.aiqaos.core.contract;

/**
 * LRN-4: the {@link SafeAdoptionGate}'s decision for one {@link AdoptionCandidate} — the verdict, a
 * human-readable reason (naming the failed dimension on rejection), and the underlying
 * {@link ConfidenceVerdict} for traceability.
 */
public class AdoptionDecision {

    private final String candidateId;
    private final AdoptionVerdict verdict;
    private final String reason;
    private final ConfidenceVerdict confidenceVerdict;

    public AdoptionDecision(String candidateId, AdoptionVerdict verdict, String reason,
                            ConfidenceVerdict confidenceVerdict) {
        this.candidateId = candidateId;
        this.verdict = verdict;
        this.reason = reason;
        this.confidenceVerdict = confidenceVerdict;
    }

    public static AdoptionDecision admitted(String candidateId, ConfidenceVerdict confidenceVerdict,
                                            String reason) {
        return new AdoptionDecision(candidateId, AdoptionVerdict.ADMITTED, reason, confidenceVerdict);
    }

    public static AdoptionDecision rejected(String candidateId, ConfidenceVerdict confidenceVerdict,
                                            String reason) {
        return new AdoptionDecision(candidateId, AdoptionVerdict.REJECTED_FOR_REVIEW, reason,
                confidenceVerdict);
    }

    public boolean isAdmitted() {
        return verdict == AdoptionVerdict.ADMITTED;
    }

    public String getCandidateId() { return candidateId; }
    public AdoptionVerdict getVerdict() { return verdict; }
    public String getReason() { return reason; }
    public ConfidenceVerdict getConfidenceVerdict() { return confidenceVerdict; }

    @Override
    public String toString() {
        return "AdoptionDecision{" + candidateId + " → " + verdict + " (" + reason + ")}";
    }
}
