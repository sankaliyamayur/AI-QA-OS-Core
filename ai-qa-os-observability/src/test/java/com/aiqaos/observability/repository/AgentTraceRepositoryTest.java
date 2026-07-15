package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.AgentTraceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class AgentTraceRepositoryTest {

    @Autowired
    private AgentTraceRepository repository;

    @Test
    void storesAndRetrievesFullPromptAndResponse() {
        AgentTraceEntity trace = new AgentTraceEntity();
        trace.setCorrelationId("corr-123");
        trace.setAgentType("QA_ANALYST");
        trace.setProvider("openai");
        trace.setModel("gpt-4o-mini");
        trace.setPrompt("Analyze this user story...");
        trace.setResponse("{\"analysisSummary\":\"...\"}");
        trace.setPromptTokens(100L);
        trace.setCompletionTokens(50L);
        trace.setLatencyMs(300L);
        trace.setTimestamp(LocalDateTime.now());
        repository.save(trace);

        List<AgentTraceEntity> byCorrelation = repository.findByCorrelationId("corr-123");
        assertEquals(1, byCorrelation.size());
        assertEquals("Analyze this user story...", byCorrelation.get(0).getPrompt());

        assertEquals(1, repository.findByCorrelationIdIn(List.of("corr-123", "nonexistent")).size());
        assertEquals(1, repository.findTop50ByOrderByTimestampDesc().size());
    }
}
