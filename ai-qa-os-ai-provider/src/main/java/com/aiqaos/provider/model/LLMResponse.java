package com.aiqaos.provider.model;

public class LLMResponse {
    private final String text;
    private final String model;
    private final TokenUsage usage;
    private final long latencyMs;

    public LLMResponse(String text, String model, TokenUsage usage, long latencyMs) {
        this.text = text;
        this.model = model;
        this.usage = usage;
        this.latencyMs = latencyMs;
    }

    public String getText() { return text; }
    public String getModel() { return model; }
    public TokenUsage getUsage() { return usage; }
    public long getLatencyMs() { return latencyMs; }
}