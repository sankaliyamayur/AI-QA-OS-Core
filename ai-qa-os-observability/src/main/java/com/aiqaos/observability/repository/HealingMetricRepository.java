package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.HealingMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealingMetricRepository extends JpaRepository<HealingMetricEntity, UUID> {
    List<HealingMetricEntity> findByExecutionId(UUID executionId);
    List<HealingMetricEntity> findByActionType(String actionType);
    List<HealingMetricEntity> findByRecoveryStatus(String recoveryStatus);
}
