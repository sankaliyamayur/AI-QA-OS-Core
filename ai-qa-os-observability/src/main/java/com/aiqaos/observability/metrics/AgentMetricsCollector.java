package com.aiqaos.observability.metrics;

import org.springframework.stereotype.Component;

@Component
public class AgentMetricsCollector {

    private final MetricsCollector collector;

    public AgentMetricsCollector(MetricsCollector collector) {
        this.collector = collector;
    }

    public void recordAgentAction(String agentType, String action, String result, long durationMs) {
        collector.incrementCounter("qaos.agent.actions", "agentType", agentType, "action", action, "result", result);
        collector.recordTime("qaos.agent.action.duration", durationMs, "agentType", agentType, "action", action);
    }
}