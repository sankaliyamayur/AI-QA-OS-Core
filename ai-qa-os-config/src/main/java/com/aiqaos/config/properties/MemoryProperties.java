package com.aiqaos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiqaos.memory")
public class MemoryProperties {
    private String storageType = "vector"; // in-memory, database, vector
    private int shortTermLimit = 10;
    private int longTermLimit = 100;

    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }
    public int getShortTermLimit() { return shortTermLimit; }
    public void setShortTermLimit(int shortTermLimit) { this.shortTermLimit = shortTermLimit; }
    public int getLongTermLimit() { return longTermLimit; }
    public void setLongTermLimit(int longTermLimit) { this.longTermLimit = longTermLimit; }
}