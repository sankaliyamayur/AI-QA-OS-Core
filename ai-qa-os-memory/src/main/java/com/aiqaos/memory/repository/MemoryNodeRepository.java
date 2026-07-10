package com.aiqaos.memory.repository;

import com.aiqaos.memory.entity.MemoryNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MemoryNodeRepository extends JpaRepository<MemoryNodeEntity, UUID> {
    List<MemoryNodeEntity> findByOwnerId(String ownerId);
    List<MemoryNodeEntity> findByMemoryType(String memoryType);
}