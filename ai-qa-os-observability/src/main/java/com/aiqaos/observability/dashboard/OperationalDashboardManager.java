package com.aiqaos.observability.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OBS-3: Operational Dashboard Manager.
 * Manages Grafana operational dashboard templates and export lifecycle.
 */
@Component
public class OperationalDashboardManager {

    private static final Logger log = LoggerFactory.getLogger(OperationalDashboardManager.class);

    private final GrafanaDashboardExporter exporter;

    public OperationalDashboardManager(GrafanaDashboardExporter exporter) {
        this.exporter = exporter;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            exporter.exportDashboardsToDirectory("deployment/dashboards");
            log.info("OBS-3: Operational Dashboard Manager initialized and exported Grafana templates.");
        } catch (Exception e) {
            log.warn("OBS-3: Operational Dashboard Manager export warning: {}", e.getMessage());
        }
    }

    public Map<String, String> getDashboardTemplates() {
        Map<String, String> templates = new HashMap<>();
        templates.put("overview", exporter.generateOverviewDashboardJson());
        templates.put("agents", exporter.generateAgentsDashboardJson());
        return templates;
    }
}
