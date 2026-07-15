package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.TimelineEventEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class TimelineEventRepositoryTest {

    @Autowired
    private TimelineEventRepository repository;

    @Test
    void ordersByExecutionAndSequenceNumber() {
        UUID executionId = UUID.randomUUID();

        saveEvent(executionId, 2, "STEP_COMPLETED");
        saveEvent(executionId, 1, "STEP_STARTED");
        saveEvent(executionId, 3, "STEP_STARTED");

        List<TimelineEventEntity> ordered = repository.findByExecutionIdOrderBySequenceNumberAsc(executionId);
        assertEquals(3, ordered.size());
        assertEquals(1, ordered.get(0).getSequenceNumber());
        assertEquals(2, ordered.get(1).getSequenceNumber());
        assertEquals(3, ordered.get(2).getSequenceNumber());

        assertEquals(3, repository.countByExecutionId(executionId));
    }

    private void saveEvent(UUID executionId, int sequenceNumber, String eventType) {
        TimelineEventEntity entity = new TimelineEventEntity();
        entity.setExecutionId(executionId);
        entity.setCorrelationId("corr-" + executionId);
        entity.setSequenceNumber(sequenceNumber);
        entity.setEventType(eventType);
        entity.setStatus("RUNNING");
        entity.setOccurredAt(LocalDateTime.now());
        repository.save(entity);
    }
}
