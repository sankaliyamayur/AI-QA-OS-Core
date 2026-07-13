package com.aiqaos.memory.collection;

import com.aiqaos.memory.vector.VectorStoreClient;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CollectionManager {

    private final VectorStoreClient vectorStoreClient;
    private final List<String> defaultCollections = List.of(
        "requirements", "testcases", "automation", "reports", "bugs", "knowledge", "memory"
    );

    public CollectionManager(VectorStoreClient vectorStoreClient) {
        this.vectorStoreClient = vectorStoreClient;
        initializeDefaultCollections();
    }

    private void initializeDefaultCollections() {
        for (String c : defaultCollections) {
            if (!vectorStoreClient.collectionExists(c)) {
                vectorStoreClient.createCollection(c);
            }
        }
    }

    public List<String> getCollections() {
        return defaultCollections;
    }
}