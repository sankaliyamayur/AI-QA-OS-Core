package com.aiqaos.healing.locator;

/**
 * HEAL-1: locator strategies, ordered most→least robust, each with a base robustness confidence.
 * A {@code data-testid} is the most stable anchor; an absolute {@code xpath} the most brittle. The
 * heuristic healer prefers higher-confidence strategies when proposing an alternative locator.
 */
public enum LocatorStrategy {
    TEST_ID(0.95),
    ID(0.90),
    NAME(0.85),
    ROLE(0.80),
    TEXT(0.70),
    CSS(0.55),
    XPATH(0.35);

    private final double baseConfidence;

    LocatorStrategy(double baseConfidence) {
        this.baseConfidence = baseConfidence;
    }

    public double baseConfidence() {
        return baseConfidence;
    }
}
