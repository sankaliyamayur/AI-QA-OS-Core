package com.aiqaos.healing.locator;

/**
 * HEAL-1: a proposed replacement locator — its concrete selector {@code value}, the
 * {@link LocatorStrategy} it uses, a robustness {@code confidence}, and why it was proposed.
 */
public final class LocatorCandidate {

    private final String value;
    private final LocatorStrategy strategy;
    private final double confidence;
    private final String rationale;

    public LocatorCandidate(String value, LocatorStrategy strategy, double confidence, String rationale) {
        this.value = value;
        this.strategy = strategy;
        this.confidence = confidence;
        this.rationale = rationale;
    }

    public String getValue() { return value; }
    public LocatorStrategy getStrategy() { return strategy; }
    public double getConfidence() { return confidence; }
    public String getRationale() { return rationale; }

    @Override
    public String toString() {
        return "LocatorCandidate{" + strategy + " '" + value + "' @" + confidence + "}";
    }
}
