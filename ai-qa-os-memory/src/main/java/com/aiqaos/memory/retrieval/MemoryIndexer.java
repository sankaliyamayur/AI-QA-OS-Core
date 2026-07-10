package com.aiqaos.memory.retrieval;

import com.aiqaos.core.provider.EmbeddingProvider;
import com.aiqaos.memory.vector.VectorStoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class MemoryIndexer {

    @Autowired(required = false)
    private EmbeddingProvider embeddingProvider;

    @Autowired
    private VectorStoreClient vectorStoreClient;

    public void indexContent(String id, String content, Map<String, Object> metadata) {
        float[] embedding = null;
        if (embeddingProvider != null) {
            embedding = embeddingProvider.embed(content);
        } else {
            embedding = new float[1536]; // Stub dimension
        }
        vectorStoreClient.save(id, embedding, metadata, content);
    }
}