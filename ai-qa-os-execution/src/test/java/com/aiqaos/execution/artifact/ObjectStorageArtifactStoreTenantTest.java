package com.aiqaos.execution.artifact;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FI-ENT1-E (ADR-056): artifact blobs are namespaced by tenant, so one tenant cannot read another's
 * bytes by key. Validated against the in-memory object-storage client (the real MinIO client is
 * deferred, ADR-053).
 */
class ObjectStorageArtifactStoreTenantTest {

    private final InMemoryObjectStorageClient client = new InMemoryObjectStorageClient();
    private final ObjectStorageArtifactStore store = new ObjectStorageArtifactStore(client, "artifacts/");

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void tenantsCannotReadEachOthersArtifacts() {
        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        store.store("run-1/shot.png", new byte[]{1, 2, 3});
        assertTrue(store.exists("run-1/shot.png"));
        assertArrayEquals(new byte[]{1, 2, 3}, store.resolve("run-1/shot.png"));
        assertTrue(store.list("run-1/").contains("run-1/shot.png"));

        // A different tenant, same key → fully isolated.
        TenantContextHolder.set(TenantContext.ofTenant("beta"));
        assertFalse(store.exists("run-1/shot.png"), "beta must not see acme's artifact");
        assertThrows(NoSuchElementException.class, () -> store.resolve("run-1/shot.png"));
        assertTrue(store.list("run-1/").isEmpty(), "beta's listing must be empty");

        // beta stores at the same key without collision.
        store.store("run-1/shot.png", new byte[]{9});
        assertArrayEquals(new byte[]{9}, store.resolve("run-1/shot.png"));

        // acme still sees only its own bytes.
        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        assertArrayEquals(new byte[]{1, 2, 3}, store.resolve("run-1/shot.png"));
    }

    @Test
    void unboundContextUsesSystemTenantNamespace() {
        TenantContextHolder.clear();
        store.store("k", new byte[]{7});
        assertTrue(store.exists("k"));
        assertTrue(client.exists("artifacts/__system__/k"), "unbound work lands under the system tenant");
    }
}
