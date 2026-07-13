package com.aiqaos.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "platform")
public class PlatformProperties {
    private int retries = 3;
    private boolean enableLearning = true;
    private boolean enableTracing = true;
    private boolean enableMetrics = true;
    private boolean enableAIFallback = true;

    public int getRetries() { return retries; }
    public void setRetries(int retries) { this.retries = retries; }
    public boolean isEnableLearning() { return enableLearning; }
    public void setEnableLearning(boolean enableLearning) { this.enableLearning = enableLearning; }
    public boolean isEnableTracing() { return enableTracing; }
    public void setEnableTracing(boolean enableTracing) { this.enableTracing = enableTracing; }
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    public boolean isEnableAIFallback() { return enableAIFallback; }
    public void setEnableAIFallback(boolean enableAIFallback) { this.enableAIFallback = enableAIFallback; }
}