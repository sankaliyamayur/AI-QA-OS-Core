package com.aiqaos.observability.metrics;

import org.springframework.stereotype.Component;

@Component
public class WorkflowMetricsCollector {

    private final MetricsCollector collector;

    public WorkflowMetricsCollector(MetricsCollector collector) {
        this.collector = collector;
    }

    public void recordWorkflowRun(String workflowName, String status, long durationMs) {
        collector.incrementCounter("qaos.workflow.runs", "workflow", workflowName, "status", status);
        collector.recordTime("qaos.workflow.duration", durationMs, "workflow", workflowName);
    }
}