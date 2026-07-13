package com.aiqaos.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsCollector {

    private final MeterRegistry registry;

    public MetricsCollector(MeterRegistry registry) {
        this.registry = registry;
    }

    public void incrementCounter(String name, String... tags) {
        registry.counter(name, tags).increment();
    }

    public void recordTime(String name, long durationMs, String... tags) {
        registry.timer(name, tags).record(java.time.Duration.ofMillis(durationMs));
    }
}