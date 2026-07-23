package com.aiqaos.eval.benchmark;

import java.util.List;

/**
 * The pass/fail outcome of a regression benchmark across one or more suites. {@link #isPassed()}
 * is the signal the CI gate acts on; {@link #getRegressedSuites()} names what fell below baseline.
 */
public class BenchmarkVerdict {

    private final boolean passed;
    private final List<String> regressedSuites;

    public BenchmarkVerdict(boolean passed, List<String> regressedSuites) {
        this.passed = passed;
        this.regressedSuites = regressedSuites == null ? List.of() : List.copyOf(regressedSuites);
    }

    public boolean isPassed() {
        return passed;
    }

    public List<String> getRegressedSuites() {
        return regressedSuites;
    }
}
