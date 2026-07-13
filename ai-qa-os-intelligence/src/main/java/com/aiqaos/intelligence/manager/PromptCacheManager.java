package com.aiqaos.intelligence.manager;

import com.aiqaos.memory.store.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Optional;

@Component
public class PromptCacheManager {

    @Autowired
    private MemoryStore memoryStore;

    public void cachePrompt(String key, String renderedPrompt) {
        memoryStore.put("prompt:" + key, renderedPrompt, Duration.ofMinutes(15));
    }

    public Optional<String> getCachedPrompt(String key) {
        return memoryStore.get("prompt:" + key).map(String::valueOf);
    }
}