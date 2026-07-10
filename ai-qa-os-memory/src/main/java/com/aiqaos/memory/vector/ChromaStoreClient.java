package com.aiqaos.memory.vector;

import com.aiqaos.memory.model.MemoryNodeDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "aiqaos.memory.vector.provider", havingValue = "chroma")
public class ChromaStoreClient implements VectorStoreClient {
    @Override
    public void save(String id, float[] embedding, Map<String, Object> metadata, String content) {}
    @Override
    public List<MemoryNodeDTO> search(float[] queryEmbedding, int topK, Map<String, Object> metadataFilters) {
        return Collections.emptyList();
    }
    @Override
    public void delete(String id) {}
}