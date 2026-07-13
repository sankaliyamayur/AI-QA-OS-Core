package com.aiqaos.workflow.plugin;

public interface PluginStep {
    String getType();
    String execute(String input);
}