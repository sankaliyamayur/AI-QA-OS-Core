package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.AgentMetricEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AgentMetricsRepositoryTest {

    @Autowired
    private AgentMetricsRepository repository;

    @Test
    void findByExecutionIdReturnsSavedRows() {
        UUID executionId = UUID.randomUUID();

        AgentMetricEntity entity = new AgentMetricEntity();
        entity.setExecutionId(executionId);
        entity.setWorkflowId(UUID.randomUUID());
        entity.setAgentType("QAAnalysisStep");
        entity.setOperation("PIPELINE_STEP");
        entity.setDurationMs(120);
        entity.setSuccess(true);
        entity.setRecordedAt(LocalDateTime.now());
        repository.save(entity);

        AgentMetricEntity other = new AgentMetricEntity();
        other.setExecutionId(UUID.randomUUID());
        other.setAgentType("ScriptGenerationStep");
        other.setDurationMs(80);
        other.setRecordedAt(LocalDateTime.now());
        repository.save(other);

        List<AgentMetricEntity> results = repository.findByExecutionId(executionId);
        assertEquals(1, results.size());
        assertEquals("QAAnalysisStep", results.get(0).getAgentType());

        assertTrue(repository.findByAgentType("ScriptGenerationStep").size() >= 1);
    }
}
