package com.aiqaos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiqaos.execution")
public class ExecutionProperties {
    private int maxRetries = 3;
    private boolean sandboxEnabled = true;
    private String executionMode = "parallel"; // sequential, parallel

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public boolean isSandboxEnabled() { return sandboxEnabled; }
    public void setSandboxEnabled(boolean sandboxEnabled) { this.sandboxEnabled = sandboxEnabled; }
    public String getExecutionMode() { return executionMode; }
    public void setExecutionMode(String executionMode) { this.executionMode = executionMode; }
}