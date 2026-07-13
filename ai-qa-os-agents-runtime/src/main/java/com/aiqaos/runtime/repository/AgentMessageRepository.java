package com.aiqaos.runtime.repository;

import com.aiqaos.runtime.entity.AgentMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AgentMessageRepository extends JpaRepository<AgentMessageEntity, UUID> {
}