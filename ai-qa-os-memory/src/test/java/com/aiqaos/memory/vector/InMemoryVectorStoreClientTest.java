package com.aiqaos.memory.vector;

import com.aiqaos.memory.model.MemoryMetadata;
import com.aiqaos.memory.model.VectorSearchResult;
import com.aiqaos.memory.ranking.SimilarityCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryVectorStoreClientTest {

    private InMemoryVectorStoreClient client;

    @BeforeEach
    void setUp() {
        client = new InMemoryVectorStoreClient(new SimilarityCalculator());
    }

    @Test
    void testSaveAndSearch() {
        String collection = "test-collection";
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();

        float[] queryEmbedding = new float[]{1.0f, 0.0f, 0.0f};
        float[] item1Embedding = new float[]{0.9f, 0.1f, 0.0f}; // high similarity
        float[] item2Embedding = new float[]{0.1f, 0.9f, 0.0f}; // lower similarity

        MemoryMetadata meta = new MemoryMetadata();

        client.save(id1, item1Embedding, meta, "content 1", collection);
        client.save(id2, item2Embedding, meta, "content 2", collection);

        List<VectorSearchResult> results = client.search(queryEmbedding, 2, collection, meta);

        assertEquals(2, results.size());
        assertEquals(UUID.fromString(id1), results.get(0).getNode().getId());
        assertEquals("content 1", results.get(0).getNode().getContent());
        assertTrue(results.get(0).getSimilarity() > results.get(1).getSimilarity());
    }

    @Test
    void testDeleteAndUpdate() {
        String collection = "test-collection";
        String id = UUID.randomUUID().toString();
        float[] embedding = new float[]{1.0f, 1.0f};
        MemoryMetadata meta = new MemoryMetadata();

        client.save(id, embedding, meta, "initial content", collection);
        assertEquals(1, client.count(collection));

        client.update(id, embedding, meta, "updated content", collection);
        assertEquals(1, client.count(collection));

        List<VectorSearchResult> results = client.search(embedding, 1, collection, meta);
        assertEquals("updated content", results.get(0).getNode().getContent());

        client.delete(id, collection);
        assertEquals(0, client.count(collection));
    }

    @Test
    void testCollectionManagement() {
        String collection = "managed-collection";

        assertFalse(client.collectionExists(collection));
        client.createCollection(collection);
        assertTrue(client.collectionExists(collection));

        client.deleteCollection(collection);
        assertFalse(client.collectionExists(collection));
    }

    @Test
    void testHealth() {
        assertTrue(client.health());
    }
}
