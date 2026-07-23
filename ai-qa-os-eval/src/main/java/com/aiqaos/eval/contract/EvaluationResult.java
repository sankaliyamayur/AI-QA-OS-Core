package com.aiqaos.eval.contract;

/**
 * The outcome of a single {@link Evaluator} run: a normalised score in {@code [0,1]},
 * a pass/fail verdict, and a human-readable reason.
 */
public class EvaluationResult {

    private final String evaluatorName;
    private final double score;
    private final boolean passed;
    private final String reason;

    public EvaluationResult(String evaluatorName, double score, boolean passed, String reason) {
        this.evaluatorName = evaluatorName;
        this.score = clamp(score);
        this.passed = passed;
        this.reason = reason;
    }

    private static double clamp(double v) {
        if (Double.isNaN(v)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, v));
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public double getScore() {
        return score;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getReason() {
        return reason;
    }
}
