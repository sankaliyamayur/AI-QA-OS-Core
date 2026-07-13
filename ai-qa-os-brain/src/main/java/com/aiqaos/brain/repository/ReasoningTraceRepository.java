package com.aiqaos.brain.repository;

import com.aiqaos.brain.entity.ReasoningTraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReasoningTraceRepository extends JpaRepository<ReasoningTraceEntity, UUID> {
}