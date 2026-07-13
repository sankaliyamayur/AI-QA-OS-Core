package com.aiqaos.integration;

import com.aiqaos.integration.pipeline.LearningPipeline;
import com.aiqaos.memory.retrieval.MemoryRetriever;
import com.aiqaos.memory.model.VectorSearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class LearningPipelineIntegrationTest {

    @Autowired
    private LearningPipeline learningPipeline;

    @Autowired
    private MemoryRetriever memoryRetriever;

    @Test
    public void testFailedExecutionLogsIngestedToMemory() {
        UUID executionId = UUID.randomUUID();
        String errorLogs = "Playwright execution failed: locator 'btn-submit' mismatch on login page";

        learningPipeline.saveExecutionLearning(executionId, errorLogs);

        // Search memory for matching failure contexts
        List<VectorSearchResult> results = memoryRetriever.retrieveSimilar(
            "locator mismatch login", 
            5, 
            "bugs", 
            null
        );

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.getNode().getContent().contains("btn-submit")));
    }
}