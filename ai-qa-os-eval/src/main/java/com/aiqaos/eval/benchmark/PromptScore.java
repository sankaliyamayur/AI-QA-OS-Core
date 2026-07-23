package com.aiqaos.eval.benchmark;

import java.util.Map;

/**
 * The objective quality signal for one prompt version: an overall score in {@code [0,1]} (the
 * mean case score across all benchmarked suites) plus the per-suite breakdown. Computed by
 * {@link PromptBenchmarkService}; PE-3 presents it, reading MOD-3's persisted results as history.
 */
public class PromptScore {

    private final String promptRef;
    private final double overall;
    private final Map<String, Double> perSuite;
    private final int caseCount;

    public PromptScore(String promptRef, double overall, Map<String, Double> perSuite, int caseCount) {
        this.promptRef = promptRef;
        this.overall = overall;
        this.perSuite = perSuite == null ? Map.of() : Map.copyOf(perSuite);
        this.caseCount = caseCount;
    }

    public String getPromptRef() {
        return promptRef;
    }

    public double getOverall() {
        return overall;
    }

    public Map<String, Double> getPerSuite() {
        return perSuite;
    }

    public int getCaseCount() {
        return caseCount;
    }

    @Override
    public String toString() {
        return "PromptScore{promptRef=" + promptRef + ", overall=" + overall
                + ", caseCount=" + caseCount + ", perSuite=" + perSuite + '}';
    }
}
