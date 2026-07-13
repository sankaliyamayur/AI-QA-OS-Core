package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.LLMCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LLMCostRepository extends JpaRepository<LLMCostEntity, UUID> {
}