package com.aiqaos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "aiqaos.features")
public class FeatureFlagProperties {
    private Map<String, Boolean> flags = new HashMap<>();

    public Map<String, Boolean> getFlags() { return flags; }
    public void setFlags(Map<String, Boolean> flags) { this.flags = flags; }

    public boolean isEnabled(String feature) {
        return flags.getOrDefault(feature, Boolean.FALSE);
    }
}