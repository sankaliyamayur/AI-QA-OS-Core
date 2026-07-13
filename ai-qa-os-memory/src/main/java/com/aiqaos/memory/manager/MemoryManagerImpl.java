package com.aiqaos.memory.manager;

import com.aiqaos.core.contract.MemoryRequest;
import com.aiqaos.core.contract.MemoryResponse;
import com.aiqaos.core.engine.MemoryEngine;
import com.aiqaos.memory.component.MemoryContextBuilder;
import com.aiqaos.memory.model.MemoryNodeDTO;
import com.aiqaos.memory.model.MemoryMetadata;
import com.aiqaos.memory.model.VectorSearchResult;
import com.aiqaos.memory.retrieval.MemoryRetriever;
import com.aiqaos.memory.ingestion.MemoryIngestionService;
import com.aiqaos.memory.entity.MemoryNodeEntity;
import com.aiqaos.memory.repository.MemoryNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemoryManagerImpl implements MemoryEngine<MemoryRequest, MemoryResponse> {

    @Autowired
    private MemoryRetriever memoryRetriever;

    @Autowired
    private MemoryIngestionService memoryIngestionService;

    @Autowired
    private MemoryNodeRepository memoryNodeRepository;

    @Autowired
    private MemoryContextBuilder memoryContextBuilder;

    @Override
    public MemoryResponse queryMemory(MemoryRequest request) {
        MemoryMetadata metadataFilters = new MemoryMetadata();
        metadataFilters.setProject("AI-QA-OS");

        List<VectorSearchResult> searchResults = memoryRetriever.retrieveSimilar(
            request.getQuery(), request.getMaxElements(), "memory", metadataFilters
        );
        
        List<MemoryNodeDTO> nodes = searchResults.stream()
                .map(VectorSearchResult::getNode)
                .collect(Collectors.toList());

        MemoryResponse response = new MemoryResponse();
        response.getMetadata().setCorrelationId(request.getMetadata().getCorrelationId());
        response.getMetadata().setTraceId(request.getMetadata().getTraceId());
        
        List<String> contentList = nodes.stream()
                .map(MemoryNodeDTO::getContent)
                .collect(Collectors.toList());
        response.setHistoricalElements(contentList);
        
        response.setStatus("SUCCESS");
        response.setMessage(memoryContextBuilder.buildFormattedContext(nodes));
        return response;
    }

    @Override
    public void storeMemory(MemoryRequest request, MemoryResponse response) {
        UUID nodeId = UUID.randomUUID();
        
        MemoryMetadata metadata = new MemoryMetadata();
        metadata.setId(nodeId);
        metadata.setProject("AI-QA-OS");
        metadata.setDocumentType("memory");

        memoryIngestionService.ingest(request.getQuery(), metadata, "memory");
        
        MemoryNodeEntity entity = new MemoryNodeEntity();
        entity.setId(nodeId);
        entity.setContent(request.getQuery());
        entity.setMemoryType("SEMANTIC_VECTOR");
        entity.setCategory("CONVERSATION");
        memoryNodeRepository.save(entity);
    }
}