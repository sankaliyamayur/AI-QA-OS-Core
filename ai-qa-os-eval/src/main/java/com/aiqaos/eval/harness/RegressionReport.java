package com.aiqaos.eval.harness;

import java.util.List;

/**
 * The regression outcome for a whole suite. {@link #isSuitePassed()} is the pass/fail signal
 * PE-1 will turn into a CI merge-gate.
 */
public class RegressionReport {

    private final String suite;
    private final List<RegressionResult> results;

    public RegressionReport(String suite, List<RegressionResult> results) {
        this.suite = suite;
        this.results = results == null ? List.of() : List.copyOf(results);
    }

    /** True when no case regressed beyond tolerance. */
    public boolean isSuitePassed() {
        return results.stream().noneMatch(RegressionResult::isRegressed);
    }

    public long regressionCount() {
        return results.stream().filter(RegressionResult::isRegressed).count();
    }

    /** Mean delta across cases that had a baseline (0 when none did). */
    public double meanDelta() {
        return results.stream()
                .filter(r -> r.getBaselineScore() != null)
                .mapToDouble(RegressionResult::getDelta)
                .average()
                .orElse(0.0);
    }

    public String getSuite() {
        return suite;
    }

    public List<RegressionResult> getResults() {
        return results;
    }
}
