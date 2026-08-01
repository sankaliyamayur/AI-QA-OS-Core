package com.aiqaos.eval.benchmark;

/**
 * FI-PE3-B (ADR-069): one prompt version that has regressed — its recent evaluation scores dropped
 * materially below its earlier scores. All fields derive from persisted {@code eval_results}
 * ({@code score} + {@code createdTime}), never a fabricated baseline.
 *
 * @param versionId     the prompt version
 * @param baselineScore mean of the earlier (chronological first-half) window
 * @param currentScore  mean of the recent (chronological second-half) window
 * @param delta         {@code currentScore - baselineScore} (negative = worse)
 * @param sampleCount   how many results backed the comparison
 */
public record PromptRegressionSignal(
        String versionId,
        double baselineScore,
        double currentScore,
        double delta,
        int sampleCount) {
}
