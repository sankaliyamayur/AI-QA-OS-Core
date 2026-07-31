package com.aiqaos.memory.store;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.memory.model.MemoryMetadata;
import com.aiqaos.memory.model.VectorSearchResult;
import com.aiqaos.memory.ranking.SimilarityCalculator;
import com.aiqaos.memory.vector.InMemoryVectorStoreClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryTenantScopingTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        TenantContextHolder.clear();
    }

    @Test
    void caffeineMemoryStore_isolatesKeysByTenant() {
        CaffeineMemoryStore store = new CaffeineMemoryStore();

        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        store.put("cached_key", "acme_val", Duration.ofMinutes(5));

        TenantContextHolder.set(TenantContext.ofTenant("beta"));
        store.put("cached_key", "beta_val", Duration.ofMinutes(5));

        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        assertThat(store.get("cached_key")).contains("acme_val");

        TenantContextHolder.set(TenantContext.ofTenant("beta"));
        assertThat(store.get("cached_key")).contains("beta_val");
    }

    @Test
    void inMemoryVectorStoreClient_filtersSearchResultByTenant() {
        InMemoryVectorStoreClient client = new InMemoryVectorStoreClient(new SimilarityCalculator());
        float[] embedding = new float[]{1.0f, 0.0f, 0.0f};

        MemoryMetadata metaAcme = new MemoryMetadata();
        metaAcme.setTenantId("acme");

        MemoryMetadata metaBeta = new MemoryMetadata();
        metaBeta.setTenantId("beta");

        client.save(UUID.randomUUID().toString(), embedding, metaAcme, "Acme prompt text", "test_collection");
        client.save(UUID.randomUUID().toString(), embedding, metaBeta, "Beta prompt text", "test_collection");

        // Search as Acme
        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        List<VectorSearchResult> acmeResults = client.search(embedding, 10, "test_collection", null);
        assertThat(acmeResults).hasSize(1);
        assertThat(acmeResults.get(0).getNode().getContent()).isEqualTo("Acme prompt text");

        // Search as Beta
        TenantContextHolder.set(TenantContext.ofTenant("beta"));
        List<VectorSearchResult> betaResults = client.search(embedding, 10, "test_collection", null);
        assertThat(betaResults).hasSize(1);
        assertThat(betaResults.get(0).getNode().getContent()).isEqualTo("Beta prompt text");
    }
}
