package com.aiqaos.provider.model;

public class TokenUsage {
    private final long inputTokens;
    private final long outputTokens;

    public TokenUsage(long inputTokens, long outputTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    public long getInputTokens() { return inputTokens; }
    public long getOutputTokens() { return outputTokens; }
}