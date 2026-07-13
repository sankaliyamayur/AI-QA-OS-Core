package com.aiqaos.memory.vector;

import com.aiqaos.memory.model.MemoryMetadata;
import com.aiqaos.memory.model.MemoryNodeDTO;
import com.aiqaos.memory.model.VectorSearchResult;
import com.aiqaos.memory.ranking.SimilarityCalculator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "aiqaos.memory.vector.provider", matchIfMissing = true)
public class InMemoryVectorStoreClient implements VectorStoreClient {

    private final SimilarityCalculator similarityCalculator;
    private final ConcurrentHashMap<String, List<VectorItem>> storage = new ConcurrentHashMap<>();

    public InMemoryVectorStoreClient(SimilarityCalculator similarityCalculator) {
        this.similarityCalculator = similarityCalculator;
    }

    @Override
    public void save(String id, float[] embedding, MemoryMetadata metadata, String content, String collection) {
        storage.computeIfAbsent(collection, k -> new ArrayList<>())
            .add(new VectorItem(id, embedding, metadata, content));
    }

    @Override
    public List<VectorSearchResult> search(float[] queryEmbedding, int topK, String collection, MemoryMetadata filters) {
        List<VectorItem> items = storage.get(collection);
        if (items == null) return Collections.emptyList();

        List<VectorSearchResult> results = new ArrayList<>();
        for (VectorItem item : items) {
            double score = similarityCalculator.cosineSimilarity(queryEmbedding, item.embedding);
            MemoryNodeDTO node = new MemoryNodeDTO();
            node.setId(UUID.fromString(item.id));
            node.setContent(item.content);
            results.add(new VectorSearchResult(node, score, collection, item.metadata));
        }

        // Sort by similarity descending
        results.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));
        if (results.size() > topK) {
            return results.subList(0, topK);
        }
        return results;
    }

    @Override
    public void delete(String id, String collection) {
        List<VectorItem> items = storage.get(collection);
        if (items != null) {
            items.removeIf(item -> item.id.equals(id));
        }
    }

    @Override
    public void update(String id, float[] embedding, MemoryMetadata metadata, String content, String collection) {
        delete(id, collection);
        save(id, embedding, metadata, content, collection);
    }

    @Override
    public void createCollection(String collection) {
        storage.putIfAbsent(collection, new ArrayList<>());
    }

    @Override
    public void deleteCollection(String collection) {
        storage.remove(collection);
    }

    @Override
    public boolean collectionExists(String collection) {
        return storage.containsKey(collection);
    }

    @Override
    public long count(String collection) {
        List<VectorItem> items = storage.get(collection);
        return items == null ? 0 : items.size();
    }

    @Override
    public boolean health() { return true; }

    private static class VectorItem {
        final String id;
        final float[] embedding;
        final MemoryMetadata metadata;
        final String content;

        VectorItem(String id, float[] embedding, MemoryMetadata metadata, String content) {
            this.id = id;
            this.embedding = embedding;
            this.metadata = metadata;
            this.content = content;
        }
    }
}