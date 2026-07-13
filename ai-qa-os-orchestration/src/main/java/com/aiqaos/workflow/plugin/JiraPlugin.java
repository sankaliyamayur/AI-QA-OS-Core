package com.aiqaos.workflow.plugin;

import org.springframework.stereotype.Component;

@Component
public class JiraPlugin implements PluginStep {
    @Override
    public String getType() { return "Jira"; }
    @Override
    public String execute(String input) {
        return "Jira ticket created with summary: " + input;
    }
}