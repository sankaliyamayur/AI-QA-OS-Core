package com.aiqaos.observability.metrics;

import org.springframework.stereotype.Component;

@Component("observabilityExecutionMetricsCollector")
public class ExecutionMetricsCollector {

    private final MetricsCollector collector;

    public ExecutionMetricsCollector(MetricsCollector collector) {
        this.collector = collector;
    }

    public void recordExecution(String status, long durationMs) {
        collector.incrementCounter("qaos.execution.total", "status", status);
        collector.recordTime("qaos.execution.duration", durationMs, "status", status);
    }
}