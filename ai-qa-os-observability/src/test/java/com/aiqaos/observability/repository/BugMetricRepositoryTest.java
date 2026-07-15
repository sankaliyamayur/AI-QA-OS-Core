package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.BugMetricEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BugMetricRepositoryTest {

    @Autowired
    private BugMetricRepository repository;

    @Test
    void findByExecutionIdAndFailureCategory() {
        UUID executionId = UUID.randomUUID();

        BugMetricEntity bug = new BugMetricEntity();
        bug.setExecutionId(executionId);
        bug.setFailureCategory("ELEMENT_NOT_FOUND");
        bug.setSeverity("HIGH");
        bug.setPriority("P1");
        bug.setConfidence(0.9);
        bug.setDetectedAt(LocalDateTime.now());
        repository.save(bug);

        List<BugMetricEntity> byExecution = repository.findByExecutionId(executionId);
        assertEquals(1, byExecution.size());

        List<BugMetricEntity> byCategory = repository.findByFailureCategory("ELEMENT_NOT_FOUND");
        assertEquals(1, byCategory.size());
    }
}
