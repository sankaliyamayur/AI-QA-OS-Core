package com.aiqaos.memory.component;

import com.aiqaos.memory.entity.MemoryNodeEntity;
import com.aiqaos.memory.repository.MemoryNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MemoryPersistence {

    @Autowired
    private MemoryNodeRepository memoryNodeRepository;

    @Async("taskExecutor")
    public void saveNodeAsynchronously(MemoryNodeEntity entity) {
        memoryNodeRepository.save(entity);
    }
}