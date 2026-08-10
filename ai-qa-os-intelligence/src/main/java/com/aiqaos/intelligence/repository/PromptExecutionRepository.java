package com.aiqaos.intelligence.repository;

import com.aiqaos.intelligence.entity.PromptExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PromptExecutionRepository extends JpaRepository<PromptExecutionEntity, UUID> {

    /**
     * FI-PE3-C: every prompt rendered during one workflow run, newest first. Keyed on MNT-6's
     * pipeline correlationId, which the recorder captures from the MDC. Ordering and filtering are
     * done by the database — the history table grows by one row per render, so it must never be
     * pulled into memory to be sorted.
     */
    List<PromptExecutionEntity> findByCorrelationIdOrderByCreatedAtDesc(String correlationId);
}