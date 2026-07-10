package com.aiqaos.memory.retrieval;

import com.aiqaos.core.provider.EmbeddingProvider;
import com.aiqaos.memory.model.MemoryNodeDTO;
import com.aiqaos.memory.vector.VectorStoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MemoryRetriever {

    @Autowired(required = false)
    private EmbeddingProvider embeddingProvider;

    @Autowired
    private VectorStoreClient vectorStoreClient;

    public List<MemoryNodeDTO> retrieveSimilar(String query, int topK, Map<String, Object> filters) {
        float[] embedding = null;
        if (embeddingProvider != null) {
            embedding = embeddingProvider.embed(query);
        } else {
            embedding = new float[1536];
        }
        return vectorStoreClient.search(embedding, topK, filters);
    }
}