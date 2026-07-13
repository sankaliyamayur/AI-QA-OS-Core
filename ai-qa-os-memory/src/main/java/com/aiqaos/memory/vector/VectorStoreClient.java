package com.aiqaos.memory.vector;

import com.aiqaos.memory.model.MemoryMetadata;
import com.aiqaos.memory.model.VectorSearchResult;
import java.util.List;

public interface VectorStoreClient {
    void save(String id, float[] embedding, MemoryMetadata metadata, String content, String collection);
    List<VectorSearchResult> search(float[] queryEmbedding, int topK, String collection, MemoryMetadata filters);
    void delete(String id, String collection);
    void update(String id, float[] embedding, MemoryMetadata metadata, String content, String collection);
    void createCollection(String collection);
    void deleteCollection(String collection);
    boolean collectionExists(String collection);
    long count(String collection);
    boolean health();
}