package com.aiqaos.memory.manager;

import com.aiqaos.core.contract.MemoryRequest;
import com.aiqaos.core.contract.MemoryResponse;
import com.aiqaos.core.engine.MemoryEngine;
import com.aiqaos.memory.component.MemoryContextBuilder;
import com.aiqaos.memory.model.MemoryNodeDTO;
import com.aiqaos.memory.retrieval.MemoryRetriever;
import com.aiqaos.memory.retrieval.MemoryIndexer;
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
    private MemoryIndexer memoryIndexer;

    @Autowired
    private MemoryNodeRepository memoryNodeRepository;

    @Autowired
    private MemoryContextBuilder memoryContextBuilder;

    @Override
    public MemoryResponse queryMemory(MemoryRequest request) {
        List<MemoryNodeDTO> nodes = memoryRetriever.retrieveSimilar(request.getQuery(), request.getMaxElements(), null);
        
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
        memoryIndexer.indexContent(nodeId.toString(), request.getQuery(), null);
        
        MemoryNodeEntity entity = new MemoryNodeEntity();
        entity.setId(nodeId);
        entity.setContent(request.getQuery());
        entity.setMemoryType("SEMANTIC_VECTOR");
        entity.setCategory("CONVERSATION");
        memoryNodeRepository.save(entity);
    }
}