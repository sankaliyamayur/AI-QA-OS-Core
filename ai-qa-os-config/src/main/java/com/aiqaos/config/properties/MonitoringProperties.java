package com.aiqaos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiqaos.monitoring")
public class MonitoringProperties {
    private boolean prometheusEnabled = true;
    private boolean healthDetailsVisible = true;

    public boolean isPrometheusEnabled() { return prometheusEnabled; }
    public void setPrometheusEnabled(boolean prometheusEnabled) { this.prometheusEnabled = prometheusEnabled; }
    public boolean isHealthDetailsVisible() { return healthDetailsVisible; }
    public void setHealthDetailsVisible(boolean healthDetailsVisible) { this.healthDetailsVisible = healthDetailsVisible; }
}