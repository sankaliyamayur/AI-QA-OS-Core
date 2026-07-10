package com.aiqaos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiqaos.workflow")
public class WorkflowProperties {
    private int maxConcurrentRuns = 10;
    private int timeoutMinutes = 60;
    private String defaultEngine = "standard";

    public int getMaxConcurrentRuns() { return maxConcurrentRuns; }
    public void setMaxConcurrentRuns(int maxConcurrentRuns) { this.maxConcurrentRuns = maxConcurrentRuns; }
    public int getTimeoutMinutes() { return timeoutMinutes; }
    public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
    public String getDefaultEngine() { return defaultEngine; }
    public void setDefaultEngine(String defaultEngine) { this.defaultEngine = defaultEngine; }
}