package com.aiqaos.memory.ingestion;

import com.aiqaos.core.provider.EmbeddingModel;
import com.aiqaos.core.provider.EmbeddingProvider;
import com.aiqaos.memory.chunking.DocumentChunker;
import com.aiqaos.memory.model.MemoryMetadata;
import com.aiqaos.memory.vector.VectorStoreClient;
import org.springframework.stereotype.Service;

import com.aiqaos.memory.embedding.EmbeddingCacheManager;
import java.util.List;
import java.util.UUID;

@Service
public class MemoryIngestionService {

    private final DocumentChunker chunker;
    private final EmbeddingProvider embeddingProvider;
    private final VectorStoreClient vectorStoreClient;
    private final EmbeddingCacheManager cacheManager;

    public MemoryIngestionService(DocumentChunker chunker,
                                  EmbeddingProvider embeddingProvider,
                                  VectorStoreClient vectorStoreClient,
                                  EmbeddingCacheManager cacheManager) {
        this.chunker = chunker;
        this.embeddingProvider = embeddingProvider;
        this.vectorStoreClient = vectorStoreClient;
        this.cacheManager = cacheManager;
    }

    public void ingest(String text, MemoryMetadata metadata, String collection) {
        List<String> chunks = chunker.chunkText(text, 500, 50);

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            String chunkId = UUID.randomUUID().toString();
            
            // Check cache
            String hash = cacheManager.computeHash(chunk);
            float[] embedding = cacheManager.getCachedEmbedding(hash);
            
            if (embedding == null) {
                embedding = embeddingProvider.embed(chunk, EmbeddingModel.OPENAI_TEXT_EMBEDDING_3_SMALL);
            }
            
            // Adjust metadata chunk index values
            metadata.getAttributes().put("chunk_index", i);
            metadata.getAttributes().put("chunk_total", chunks.size());

            vectorStoreClient.save(chunkId, embedding, metadata, chunk, collection);
        }
    }
}