package com.aiqaos.workflow.plugin;

import org.springframework.stereotype.Component;

@Component
public class SlackPlugin implements PluginStep {
    @Override
    public String getType() { return "Slack"; }
    @Override
    public String execute(String input) {
        return "Slack message dispatched: " + input;
    }
}