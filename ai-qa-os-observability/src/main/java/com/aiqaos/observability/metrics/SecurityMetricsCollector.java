package com.aiqaos.observability.metrics;

import org.springframework.stereotype.Component;

@Component
public class SecurityMetricsCollector {

    private final MetricsCollector collector;

    public SecurityMetricsCollector(MetricsCollector collector) {
        this.collector = collector;
    }

    public void recordAuthEvent(String eventType, String result) {
        collector.incrementCounter("qaos.security.auth", "type", eventType, "result", result);
    }
}