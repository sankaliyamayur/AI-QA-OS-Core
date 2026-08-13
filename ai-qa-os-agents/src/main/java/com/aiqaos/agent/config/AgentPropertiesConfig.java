package com.aiqaos.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "qa")
public class AgentPropertiesConfig {

    private Map<String, String> prompts = new HashMap<>();
    private Map<String, Integer> maxTokens = new HashMap<>();

    public Map<String, String> getPrompts() { return prompts; }
    public void setPrompts(Map<String, String> prompts) { this.prompts = prompts; }

    public Map<String, Integer> getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Map<String, Integer> maxTokens) { this.maxTokens = maxTokens; }
}
