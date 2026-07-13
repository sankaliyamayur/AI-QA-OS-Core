package com.aiqaos.runtime.security;

public class AgentExecutionBudget {
    private int maxIterations = 20;
    private long maxTokens = 5000;
    private long maxExecutionTimeMs = 1800000; // 30 minutes
    private double maxCostLimit = 10.0; // $10 limit

    public int getMaxIterations() { return maxIterations; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
    public long getMaxTokens() { return maxTokens; }
    public void setMaxTokens(long maxTokens) { this.maxTokens = maxTokens; }
    public long getMaxExecutionTimeMs() { return maxExecutionTimeMs; }
    public void setMaxExecutionTimeMs(long maxExecutionTimeMs) { this.maxExecutionTimeMs = maxExecutionTimeMs; }
    public double getMaxCostLimit() { return maxCostLimit; }
    public void setMaxCostLimit(double maxCostLimit) { this.maxCostLimit = maxCostLimit; }
}
