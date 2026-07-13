package com.aiqaos.memory.vector;

import com.aiqaos.memory.model.MemoryMetadata;
import com.aiqaos.memory.model.VectorSearchResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@ConditionalOnProperty(name = "aiqaos.memory.vector.provider", havingValue = "qdrant")
public class QdrantStoreClient implements VectorStoreClient {

    @Override
    public void save(String id, float[] embedding, MemoryMetadata metadata, String content, String collection) {}

    @Override
    public List<VectorSearchResult> search(float[] queryEmbedding, int topK, String collection, MemoryMetadata filters) {
        return Collections.emptyList();
    }

    @Override
    public void delete(String id, String collection) {}

    @Override
    public void update(String id, float[] embedding, MemoryMetadata metadata, String content, String collection) {}

    @Override
    public void createCollection(String collection) {}

    @Override
    public void deleteCollection(String collection) {}

    @Override
    public boolean collectionExists(String collection) { return false; }

    @Override
    public long count(String collection) { return 0; }

    @Override
    public boolean health() { return true; }
}