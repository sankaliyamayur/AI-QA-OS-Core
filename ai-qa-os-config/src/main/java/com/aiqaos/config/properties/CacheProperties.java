package com.aiqaos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiqaos.cache")
public class CacheProperties {
    private String provider = "caffeine";
    private int ttlSeconds = 3600;
    private int maxSize = 1000;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public int getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(int ttlSeconds) { this.ttlSeconds = ttlSeconds; }
    public int getMaxSize() { return maxSize; }
    public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
}