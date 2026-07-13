package com.aiqaos.workflow.plugin;

import org.springframework.stereotype.Component;

@Component
public class GithubPlugin implements PluginStep {
    @Override
    public String getType() { return "Github"; }
    @Override
    public String execute(String input) {
        return "Github commit/issue triggered: " + input;
    }
}