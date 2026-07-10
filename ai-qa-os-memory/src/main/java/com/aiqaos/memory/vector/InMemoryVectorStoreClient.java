package com.aiqaos.memory.vector;

import com.aiqaos.memory.model.MemoryNodeDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "aiqaos.memory.vector.provider", havingValue = "inmemory", matchIfMissing = true)
public class InMemoryVectorStoreClient implements VectorStoreClient {
    private final Map<String, MemoryNodeDTO> store = new ConcurrentHashMap<>();

    @Override
    public void save(String id, float[] embedding, Map<String, Object> metadata, String content) {
        MemoryNodeDTO dto = new MemoryNodeDTO();
        dto.setId(UUID.fromString(id));
        dto.setContent(content);
        dto.setMemoryType("SEMANTIC_VECTOR");
        if (metadata != null) {
            metadata.forEach((k, v) -> dto.getMetadata().put(k, String.valueOf(v)));
        }
        store.put(id, dto);
    }

    @Override
    public List<MemoryNodeDTO> search(float[] queryEmbedding, int topK, Map<String, Object> metadataFilters) {
        List<MemoryNodeDTO> results = new ArrayList<>(store.values());
        if (results.size() > topK) {
            return results.subList(0, topK);
        }
        return results;
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }
}