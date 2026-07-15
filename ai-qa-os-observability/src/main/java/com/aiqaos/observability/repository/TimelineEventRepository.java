package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.TimelineEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEventEntity, UUID> {
    List<TimelineEventEntity> findByExecutionIdOrderBySequenceNumberAsc(UUID executionId);
    int countByExecutionId(UUID executionId);
    List<TimelineEventEntity> findByCorrelationId(String correlationId);
}
