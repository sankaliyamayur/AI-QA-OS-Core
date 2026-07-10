package com.aiqaos.memory.vector;

import java.util.List;
import java.util.Map;
import com.aiqaos.memory.model.MemoryNodeDTO;

public interface VectorStoreClient {
    void save(String id, float[] embedding, Map<String, Object> metadata, String content);
    List<MemoryNodeDTO> search(float[] queryEmbedding, int topK, Map<String, Object> metadataFilters);
    void delete(String id);
}