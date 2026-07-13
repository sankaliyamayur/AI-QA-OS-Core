package com.aiqaos.agent.repository;

import com.aiqaos.agent.entity.AgentExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AgentExecutionRepository extends JpaRepository<AgentExecutionEntity, UUID> {
}