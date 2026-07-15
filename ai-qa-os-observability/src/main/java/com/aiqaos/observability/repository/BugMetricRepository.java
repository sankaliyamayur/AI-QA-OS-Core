package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.BugMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BugMetricRepository extends JpaRepository<BugMetricEntity, UUID> {
    List<BugMetricEntity> findByExecutionId(UUID executionId);
    List<BugMetricEntity> findByFailureCategory(String failureCategory);
}
