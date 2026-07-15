package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.AgentTraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentTraceRepository extends JpaRepository<AgentTraceEntity, UUID> {
    List<AgentTraceEntity> findByCorrelationId(String correlationId);
    List<AgentTraceEntity> findByCorrelationIdIn(List<String> correlationIds);
    List<AgentTraceEntity> findTop50ByOrderByTimestampDesc();
}
