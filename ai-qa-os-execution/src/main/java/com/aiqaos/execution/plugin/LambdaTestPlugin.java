package com.aiqaos.execution.plugin;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class LambdaTestPlugin implements ExecutionPlugin {
    @Override
    public String getPluginName() { return "LambdaTest"; }
    @Override
    public void configure(Map<String, Object> capabilities) {
        capabilities.put("lambdatest.grid", "true");
    }
}