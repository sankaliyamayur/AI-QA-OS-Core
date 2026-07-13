package com.aiqaos.execution.component;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class CapabilityResolver {
    public Map<String, Object> resolveCapabilities(String browser, String environment) {
        Map<String, Object> caps = new HashMap<>();
        caps.put("browserName", browser != null ? browser : "Chrome");
        caps.put("environment", environment != null ? environment : "local");
        return caps;
    }
}