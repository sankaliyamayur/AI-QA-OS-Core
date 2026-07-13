package com.aiqaos.runtime.repository;

import com.aiqaos.runtime.entity.AgentRuntimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AgentRuntimeRepository extends JpaRepository<AgentRuntimeEntity, UUID> {
}