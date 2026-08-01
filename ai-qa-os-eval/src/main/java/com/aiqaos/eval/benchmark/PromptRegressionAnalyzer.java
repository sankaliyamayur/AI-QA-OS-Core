package com.aiqaos.eval.benchmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * FI-PE3-B (ADR-069): detects prompt versions that have <b>regressed over time</b> — the same
 * version's recent evaluation scores dropping materially below its earlier scores. Pure — no I/O — so
 * it is trivially unit-testable (the PE-3/HEAL-3 assembler pattern).
 *
 * <p>Faithfulness (ADR-063): the only inputs are each version's scores in chronological order. A
 * version with fewer than {@code minSamples} results is <b>skipped</b> — never flagged against a
 * fabricated baseline. "Regression" here is strictly temporal decline within one version, not a
 * gap to some other version.
 */
@Component
public class PromptRegressionAnalyzer {

    /**
     * @param chronologicalScoresByVersion each version's scores, already ordered oldest→newest
     * @param tolerance  a version is flagged only if its recent-window mean fell below its
     *                   earlier-window mean by more than this (e.g. 0.05)
     * @param minSamples minimum results a version needs to be judged (split into two windows)
     */
    public PromptRegressionReport analyze(Map<String, List<Double>> chronologicalScoresByVersion,
                                          double tolerance, int minSamples) {
        if (chronologicalScoresByVersion == null || chronologicalScoresByVersion.isEmpty()) {
            return PromptRegressionReport.empty(tolerance);
        }
        int effectiveMin = Math.max(2, minSamples); // need at least one result per window

        List<PromptRegressionSignal> signals = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : chronologicalScoresByVersion.entrySet()) {
            List<Double> scores = entry.getValue();
            if (scores == null || scores.size() < effectiveMin) {
                continue; // insufficient data — skip, do not fabricate a baseline
            }
            int split = scores.size() / 2;
            double earlier = mean(scores.subList(0, split));
            double recent = mean(scores.subList(split, scores.size()));
            double delta = recent - earlier;
            // Flag only when the drop exceeds tolerance beyond floating-point noise: a drop of exactly
            // tolerance is NOT a regression (e.g. 0.80 -> 0.75 with tolerance 0.05).
            if ((earlier - recent) > tolerance + 1e-9) {
                signals.add(new PromptRegressionSignal(
                        entry.getKey(), earlier, recent, delta, scores.size()));
            }
        }
        signals.sort(Comparator.comparingDouble(PromptRegressionSignal::delta)); // worst (most negative) first
        return new PromptRegressionReport(tolerance, signals.size(), signals);
    }

    private static double mean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
