package com.aiqaos.learning.analysis;

public class FlakyTestReport {

    public enum Recommendation {
        STABLE,
        FLAKY_RETRY,
        QUARANTINE
    }

    private String stepName;
    private int totalExecutions;
    private int failureCount;
    private int flipCount;
    private double flakinessScore;
    private Recommendation recommendation;

    public FlakyTestReport() {
    }

    public FlakyTestReport(String stepName, int totalExecutions, int failureCount, int flipCount, double flakinessScore, Recommendation recommendation) {
        this.stepName = stepName;
        this.totalExecutions = totalExecutions;
        this.failureCount = failureCount;
        this.flipCount = flipCount;
        this.flakinessScore = flakinessScore;
        this.recommendation = recommendation;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public int getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(int totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getFlipCount() {
        return flipCount;
    }

    public void setFlipCount(int flipCount) {
        this.flipCount = flipCount;
    }

    public double getFlakinessScore() {
        return flakinessScore;
    }

    public void setFlakinessScore(double flakinessScore) {
        this.flakinessScore = flakinessScore;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }
}
