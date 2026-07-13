package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.ObservabilityMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MetricRepository extends JpaRepository<ObservabilityMetricEntity, UUID> {
}