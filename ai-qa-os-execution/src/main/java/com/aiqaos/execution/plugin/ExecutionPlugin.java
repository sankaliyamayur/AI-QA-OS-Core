package com.aiqaos.execution.plugin;

import java.util.Map;

public interface ExecutionPlugin {
    String getPluginName();
    void configure(Map<String, Object> capabilities);
}