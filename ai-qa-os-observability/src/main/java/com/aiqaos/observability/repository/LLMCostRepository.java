package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.LLMCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LLMCostRepository extends JpaRepository<LLMCostEntity, UUID> {
    List<LLMCostEntity> findByRequestId(String requestId);
    List<LLMCostEntity> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
    List<LLMCostEntity> findTop20ByOrderByTimestampDesc();
}
