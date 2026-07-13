package com.aiqaos.observability.metrics;

import org.springframework.stereotype.Component;

@Component
public class LLMMetricsCollector {

    private final MetricsCollector collector;

    public LLMMetricsCollector(MetricsCollector collector) {
        this.collector = collector;
    }

    public void recordLLMCall(String model, long tokens, long latencyMs) {
        collector.incrementCounter("qaos.llm.tokens.used", "model", model);
        collector.recordTime("qaos.llm.latency", latencyMs, "model", model);
    }
}