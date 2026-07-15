package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.AgentMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentMetricsRepository extends JpaRepository<AgentMetricEntity, UUID> {
    List<AgentMetricEntity> findByExecutionId(UUID executionId);
    List<AgentMetricEntity> findByAgentType(String agentType);
}
