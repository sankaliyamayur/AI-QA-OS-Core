package com.aiqaos.eval.harness;

/**
 * The regression verdict for a single case: the current aggregate score against its baseline.
 */
public class RegressionResult {

    private final String caseId;
    private final double currentScore;
    private final Double baselineScore;
    private final boolean regressed;

    public RegressionResult(String caseId, double currentScore, Double baselineScore, boolean regressed) {
        this.caseId = caseId;
        this.currentScore = currentScore;
        this.baselineScore = baselineScore;
        this.regressed = regressed;
    }

    /** Current minus baseline; {@code 0.0} when there is no baseline for the case. */
    public double getDelta() {
        return baselineScore == null ? 0.0 : currentScore - baselineScore;
    }

    public String getCaseId() {
        return caseId;
    }

    public double getCurrentScore() {
        return currentScore;
    }

    public Double getBaselineScore() {
        return baselineScore;
    }

    public boolean isRegressed() {
        return regressed;
    }
}
