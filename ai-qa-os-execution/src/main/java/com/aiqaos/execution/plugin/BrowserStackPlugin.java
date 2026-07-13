package com.aiqaos.execution.plugin;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class BrowserStackPlugin implements ExecutionPlugin {
    @Override
    public String getPluginName() { return "BrowserStack"; }
    @Override
    public void configure(Map<String, Object> capabilities) {
        capabilities.put("browserstack.local", "false");
    }
}