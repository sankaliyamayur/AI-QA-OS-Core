package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.HealingMetricEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class HealingMetricRepositoryTest {

    @Autowired
    private HealingMetricRepository repository;

    @Test
    void findByExecutionIdActionTypeAndRecoveryStatus() {
        UUID executionId = UUID.randomUUID();

        HealingMetricEntity metric = new HealingMetricEntity();
        metric.setHealingId("HL-001");
        metric.setExecutionId(executionId);
        metric.setActionType("LOCATOR_UPDATE");
        metric.setHealingStrategy("LocatorRecoveryStrategy");
        metric.setHealingApplied(true);
        metric.setRetrySuccessful(true);
        metric.setRecoveryStatus("SUCCESS");
        metric.setRecordedAt(LocalDateTime.now());
        repository.save(metric);

        List<HealingMetricEntity> byExecution = repository.findByExecutionId(executionId);
        assertEquals(1, byExecution.size());

        assertEquals(1, repository.findByActionType("LOCATOR_UPDATE").size());
        assertEquals(1, repository.findByRecoveryStatus("SUCCESS").size());
    }
}
