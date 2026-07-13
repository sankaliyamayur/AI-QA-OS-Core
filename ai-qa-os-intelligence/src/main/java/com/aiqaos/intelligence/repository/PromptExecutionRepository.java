package com.aiqaos.intelligence.repository;

import com.aiqaos.intelligence.entity.PromptExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PromptExecutionRepository extends JpaRepository<PromptExecutionEntity, UUID> {
}