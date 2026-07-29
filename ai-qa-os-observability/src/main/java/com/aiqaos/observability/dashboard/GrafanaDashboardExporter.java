package com.aiqaos.observability.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * OBS-3: Operational Dashboards Suite.
 *
 * Generates and exports Grafana dashboard JSON configurations for the AI-QA-OS platform:
 *   1. AI-QA-OS Platform Overview Dashboard (throughput, pass/fail, LLM tokens, self-healing)
 *   2. AI-QA-OS Agent Performance Dashboard (per-agent latencies, confidence scores, retry counts)
 */
@Component
public class GrafanaDashboardExporter {

    private static final Logger log = LoggerFactory.getLogger(GrafanaDashboardExporter.class);

    public String generateOverviewDashboardJson() {
        return """
            {
              "annotations": { "list": [] },
              "editable": true,
              "fiscalYearStartMonth": 0,
              "graphTooltip": 1,
              "id": null,
              "title": "AI-QA-OS Platform Overview",
              "uid": "aiqaos-overview",
              "tags": ["ai-qa-os", "overview", "prometheus"],
              "timezone": "browser",
              "schemaVersion": 38,
              "version": 1,
              "panels": [
                {
                  "id": 1,
                  "title": "Pipeline Executions Throughput",
                  "type": "timeseries",
                  "gridPos": { "h": 8, "w": 12, "x": 0, "y": 0 },
                  "targets": [
                    { "expr": "sum(rate(aiqaos_pipeline_executions_total[5m])) by (status)", "legendFormat": "{{status}}" }
                  ]
                },
                {
                  "id": 2,
                  "title": "Pass Rate Gauge",
                  "type": "gauge",
                  "gridPos": { "h": 8, "w": 6, "x": 12, "y": 0 },
                  "targets": [
                    { "expr": "sum(rate(aiqaos_pipeline_executions_total{status='SUCCESS'}[5m])) / sum(rate(aiqaos_pipeline_executions_total[5m])) * 100", "legendFormat": "Pass Rate %" }
                  ]
                },
                {
                  "id": 3,
                  "title": "Self-Healing Repairs Total",
                  "type": "stat",
                  "gridPos": { "h": 8, "w": 6, "x": 18, "y": 0 },
                  "targets": [
                    { "expr": "sum(aiqaos_self_healing_repairs_total)", "legendFormat": "Repaired Locators" }
                  ]
                },
                {
                  "id": 4,
                  "title": "LLM Token Usage by Provider",
                  "type": "timeseries",
                  "gridPos": { "h": 8, "w": 12, "x": 0, "y": 8 },
                  "targets": [
                    { "expr": "sum(rate(aiqaos_llm_token_usage_total[5m])) by (provider)", "legendFormat": "{{provider}}" }
                  ]
                },
                {
                  "id": 5,
                  "title": "Pipeline Step Latency (p95)",
                  "type": "timeseries",
                  "gridPos": { "h": 8, "w": 12, "x": 12, "y": 8 },
                  "targets": [
                    { "expr": "histogram_quantile(0.95, sum(rate(aiqaos_step_duration_seconds_bucket[5m])) by (le, step))", "legendFormat": "p95 {{step}}" }
                  ]
                }
              ]
            }
            """;
    }

    public String generateAgentsDashboardJson() {
        return """
            {
              "annotations": { "list": [] },
              "editable": true,
              "title": "AI-QA-OS Agent Performance",
              "uid": "aiqaos-agents",
              "tags": ["ai-qa-os", "agents", "performance"],
              "timezone": "browser",
              "schemaVersion": 38,
              "version": 1,
              "panels": [
                {
                  "id": 1,
                  "title": "Per-Agent Execution Duration (Avg)",
                  "type": "barplot",
                  "gridPos": { "h": 8, "w": 12, "x": 0, "y": 0 },
                  "targets": [
                    { "expr": "sum(rate(aiqaos_agent_duration_seconds_sum[5m])) by (agent) / sum(rate(aiqaos_agent_duration_seconds_count[5m])) by (agent)", "legendFormat": "{{agent}}" }
                  ]
                },
                {
                  "id": 2,
                  "title": "Agent Confidence Scores",
                  "type": "timeseries",
                  "gridPos": { "h": 8, "w": 12, "x": 12, "y": 0 },
                  "targets": [
                    { "expr": "avg(aiqaos_agent_confidence_score) by (agent)", "legendFormat": "{{agent}} confidence" }
                  ]
                },
                {
                  "id": 3,
                  "title": "Flaky Test Detection Rate",
                  "type": "stat",
                  "gridPos": { "h": 8, "w": 12, "x": 0, "y": 8 },
                  "targets": [
                    { "expr": "sum(aiqaos_flaky_tests_detected_total)", "legendFormat": "Flaky Tests" }
                  ]
                },
                {
                  "id": 4,
                  "title": "Step Retry Count by Agent",
                  "type": "timeseries",
                  "gridPos": { "h": 8, "w": 12, "x": 12, "y": 8 },
                  "targets": [
                    { "expr": "sum(rate(aiqaos_step_retries_total[5m])) by (agent)", "legendFormat": "{{agent}} retries" }
                  ]
                }
              ]
            }
            """;
    }

    public String exportDashboardsToDirectory(String outputDir) throws IOException {
        Path targetDir = Path.of(outputDir != null ? outputDir : "deployment/dashboards");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        File overviewFile = targetDir.resolve("aiqaos-platform-overview-grafana.json").toFile();
        try (FileWriter writer = new FileWriter(overviewFile)) {
            writer.write(generateOverviewDashboardJson());
        }

        File agentsFile = targetDir.resolve("aiqaos-agent-performance-grafana.json").toFile();
        try (FileWriter writer = new FileWriter(agentsFile)) {
            writer.write(generateAgentsDashboardJson());
        }

        log.info("OBS-3: Exported Grafana dashboards to {}", targetDir.toAbsolutePath());
        return targetDir.toAbsolutePath().toString();
    }
}
