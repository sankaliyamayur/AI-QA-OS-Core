package com.aiqaos.orchestration.plugin;

public interface PluginStep {
    String getType();
    String execute(String input);
}