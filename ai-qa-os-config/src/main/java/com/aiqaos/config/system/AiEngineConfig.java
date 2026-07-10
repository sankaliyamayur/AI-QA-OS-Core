package com.aiqaos.config.system;

import com.aiqaos.config.properties.AiProviderProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class AiEngineConfig {

    private final AiProviderProperties aiProviderProperties;

    public AiEngineConfig(AiProviderProperties aiProviderProperties) {
        this.aiProviderProperties = aiProviderProperties;
    }

    @Bean
    public Map<String, Object> aiProviderStrategies() {
        Map<String, Object> strategies = new HashMap<>();
        strategies.put("openai", new Object());
        strategies.put("anthropic", new Object());
        strategies.put("gemini", new Object());
        strategies.put("azure", new Object());
        strategies.put("ollama", new Object());
        strategies.put("openrouter", new Object());
        return strategies;
    }

    @Bean
    public Object activeChatModel() {
        String active = aiProviderProperties.getActiveProvider();
        // Return dummy connection mock representation for compilation / strategies placeholder
        return new Object();
    }
}