package com.aiqaos.learning.metrics;

/**
 * LRN-2: one time-ordered datapoint of the learning loop's performance — the outcome of a run and
 * the confidence associated with it. A chronologically-ordered list of these is the calculator's
 * input; sourcing them from real execution history / {@code ReasoningTraceEntity} is deferred
 * (FI-LRN2-A).
 */
public final class LearningObservation {

    private final long sequence;      // ordering key (e.g. run index or epoch millis)
    private final boolean success;    // did the run pass
    private final double confidence;  // 0..1 confidence for the run
    private final String label;       // optional descriptor

    public LearningObservation(long sequence, boolean success, double confidence, String label) {
        this.sequence = sequence;
        this.success = success;
        this.confidence = confidence;
        this.label = label;
    }

    public static LearningObservation of(long sequence, boolean success, double confidence) {
        return new LearningObservation(sequence, success, confidence, null);
    }

    public long getSequence() { return sequence; }
    public boolean isSuccess() { return success; }
    public double getConfidence() { return confidence; }
    public String getLabel() { return label; }
}
