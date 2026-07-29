package com.aiqaos.observability.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GrafanaDashboardExporterTest {

    private GrafanaDashboardExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new GrafanaDashboardExporter();
    }

    @Test
    @DisplayName("OBS-3: Should generate non-empty Overview Grafana Dashboard JSON with valid Prometheus metrics")
    void testOverviewDashboardJson() {
        String json = exporter.generateOverviewDashboardJson();
        assertNotNull(json);
        assertTrue(json.contains("AI-QA-OS Platform Overview"));
        assertTrue(json.contains("aiqaos_pipeline_executions_total"));
        assertTrue(json.contains("aiqaos_llm_token_usage_total"));
        assertTrue(json.contains("aiqaos_self_healing_repairs_total"));
    }

    @Test
    @DisplayName("OBS-3: Should generate non-empty Agents Grafana Dashboard JSON")
    void testAgentsDashboardJson() {
        String json = exporter.generateAgentsDashboardJson();
        assertNotNull(json);
        assertTrue(json.contains("AI-QA-OS Agent Performance"));
        assertTrue(json.contains("aiqaos_agent_duration_seconds_sum"));
        assertTrue(json.contains("aiqaos_agent_confidence_score"));
    }

    @Test
    @DisplayName("OBS-3: Should export dashboard files to directory")
    void testExportDashboardsToDirectory(@TempDir Path tempDir) throws IOException {
        String exportedPath = exporter.exportDashboardsToDirectory(tempDir.toString());
        assertNotNull(exportedPath);

        File overviewFile = tempDir.resolve("aiqaos-platform-overview-grafana.json").toFile();
        File agentsFile = tempDir.resolve("aiqaos-agent-performance-grafana.json").toFile();

        assertTrue(overviewFile.exists(), "Overview dashboard file should exist");
        assertTrue(agentsFile.exists(), "Agents dashboard file should exist");

        String overviewContent = Files.readString(overviewFile.toPath());
        assertTrue(overviewContent.contains("aiqaos-overview"));
    }

    @Test
    @DisplayName("OBS-3: OperationalDashboardManager exposes templates")
    void testDashboardManager() {
        OperationalDashboardManager manager = new OperationalDashboardManager(exporter);
        Map<String, String> templates = manager.getDashboardTemplates();
        assertNotNull(templates);
        assertTrue(templates.containsKey("overview"));
        assertTrue(templates.containsKey("agents"));
        assertDoesNotThrow(manager::onApplicationReady);
    }
}
