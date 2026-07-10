package com.aiqaos.memory.component;

import com.aiqaos.memory.store.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MemoryCleaner {

    @Autowired
    private MemoryStore memoryStore;

    @Scheduled(fixedDelay = 3600000)
    public void cleanupExpiredMemories() {
        memoryStore.clear();
    }
}