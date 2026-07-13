package com.aiqaos.intelligence.manager;

import com.aiqaos.intelligence.model.PromptTemplateDTO;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PromptRegistry {
    private final Map<String, PromptTemplateDTO> registry = new ConcurrentHashMap<>();

    public void register(String name, PromptTemplateDTO template) {
        registry.put(name, template);
    }

    public PromptTemplateDTO get(String name) {
        return registry.get(name);
    }

    public void clear() {
        registry.clear();
    }
}