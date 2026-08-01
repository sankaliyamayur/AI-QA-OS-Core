package com.aiqaos.eval.benchmark;

import java.util.List;

/**
 * FI-PE3-B (ADR-069): the prompt-regression read-model — the versions that have declined over time,
 * worst-first, plus the tolerance the detection used. Empty {@code regressions} means none crossed
 * the threshold (or too few samples to judge — those versions are skipped, not flagged).
 *
 * @param tolerance      the score-drop threshold applied (a version is flagged only if it fell by more)
 * @param regressedCount number of regressed versions (== {@code regressions.size()})
 * @param regressions    the regressed versions, ordered by {@code delta} ascending (worst first)
 */
public record PromptRegressionReport(
        double tolerance,
        int regressedCount,
        List<PromptRegressionSignal> regressions) {

    public PromptRegressionReport {
        regressions = regressions != null ? List.copyOf(regressions) : List.of();
        regressedCount = regressions.size();
    }

    public static PromptRegressionReport empty(double tolerance) {
        return new PromptRegressionReport(tolerance, 0, List.of());
    }
}
