package com.aiqaos.execution.artifact;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalArtifactStoreTenantTest {

    @TempDir
    Path tempDir;

    private LocalArtifactStore store;

    @BeforeEach
    void setUp() {
        TenantContextHolder.clear();
        store = new LocalArtifactStore(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void storeAndResolve_isolatedByTenant() {
        byte[] contentAcme = "acme data".getBytes();
        byte[] contentBeta = "beta data".getBytes();

        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        store.store("report.txt", contentAcme);

        TenantContextHolder.set(TenantContext.ofTenant("beta"));
        store.store("report.txt", contentBeta);

        // Verify acme sees its own content
        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        assertThat(store.resolve("report.txt")).isEqualTo(contentAcme);

        // Verify beta sees its own content
        TenantContextHolder.set(TenantContext.ofTenant("beta"));
        assertThat(store.resolve("report.txt")).isEqualTo(contentBeta);
    }

    @Test
    void list_onlyReturnsCurrentTenantArtifacts() {
        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        store.store("acme-doc.txt", "doc".getBytes());

        TenantContextHolder.set(TenantContext.ofTenant("beta"));
        store.store("beta-doc.txt", "doc".getBytes());

        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        List<String> acmeFiles = store.list("");
        assertThat(acmeFiles).containsExactly("acme-doc.txt");

        TenantContextHolder.set(TenantContext.ofTenant("beta"));
        List<String> betaFiles = store.list("");
        assertThat(betaFiles).containsExactly("beta-doc.txt");
    }

    @Test
    void safeResolve_rejectsDirectoryTraversal() {
        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        assertThatThrownBy(() -> store.resolve("../other-tenant/secret.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
