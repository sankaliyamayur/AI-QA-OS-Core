package com.aiqaos.integration.pipeline;

import com.aiqaos.memory.ingestion.MemoryIngestionService;
import com.aiqaos.memory.model.MemoryMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LearningPipeline {
    private static final Logger log = LoggerFactory.getLogger(LearningPipeline.class);
    private final MemoryIngestionService memoryIngestionService;

    public LearningPipeline(MemoryIngestionService memoryIngestionService) {
        this.memoryIngestionService = memoryIngestionService;
    }

    public void saveExecutionLearning(UUID executionId, String outcomeLogs) {
        log.info("Running learning ingestion for Execution: {}", executionId);
        
        MemoryMetadata metadata = new MemoryMetadata();
        metadata.setId(executionId);
        metadata.setProject("AI-QA-OS");
        metadata.setDocumentType("bugs");

        memoryIngestionService.ingest(outcomeLogs, metadata, "bugs");
        log.info("Learning ingestion successfully written to memory collection.");
    }
}