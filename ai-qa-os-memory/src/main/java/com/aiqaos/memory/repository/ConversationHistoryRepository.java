package com.aiqaos.memory.repository;

import com.aiqaos.memory.entity.ConversationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistoryEntity, UUID> {
    List<ConversationHistoryEntity> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}