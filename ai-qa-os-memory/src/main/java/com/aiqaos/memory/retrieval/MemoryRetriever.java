package com.aiqaos.memory.retrieval;

import com.aiqaos.core.provider.EmbeddingModel;
import com.aiqaos.core.provider.EmbeddingProvider;
import com.aiqaos.memory.model.MemoryMetadata;
import com.aiqaos.memory.model.VectorSearchResult;
import com.aiqaos.memory.vector.VectorStoreClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryRetriever {

    private final EmbeddingProvider embeddingProvider;
    private final VectorStoreClient vectorStoreClient;

    public MemoryRetriever(EmbeddingProvider embeddingProvider, VectorStoreClient vectorStoreClient) {
        this.embeddingProvider = embeddingProvider;
        this.vectorStoreClient = vectorStoreClient;
    }

    public List<VectorSearchResult> retrieveSimilar(String query, int topK, String collection, MemoryMetadata filters) {
        float[] queryEmbedding = embeddingProvider.embed(query, EmbeddingModel.OPENAI_TEXT_EMBEDDING_3_SMALL);
        return vectorStoreClient.search(queryEmbedding, topK, collection, filters);
    }
}