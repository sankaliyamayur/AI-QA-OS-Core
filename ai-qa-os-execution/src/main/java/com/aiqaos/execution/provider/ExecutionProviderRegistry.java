package com.aiqaos.execution.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExecutionProviderRegistry {
    private final Map<String, ExecutionProvider> registry = new ConcurrentHashMap<>();

    @Autowired
    public ExecutionProviderRegistry(List<ExecutionProvider> providers) {
        for (ExecutionProvider provider : providers) {
            registry.put(provider.getProviderType().toUpperCase(), provider);
        }
    }

    public ExecutionProvider getProvider(String type) {
        if (type == null) return null;
        return registry.get(type.toUpperCase());
    }
}