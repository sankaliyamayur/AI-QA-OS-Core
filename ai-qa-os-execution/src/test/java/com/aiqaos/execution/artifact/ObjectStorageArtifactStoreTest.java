package com.aiqaos.execution.artifact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** ENT-5: object-storage ArtifactStore validated against the in-memory client (no cloud needed). */
class ObjectStorageArtifactStoreTest {

    private final InMemoryObjectStorageClient client = new InMemoryObjectStorageClient();
    private final ObjectStorageArtifactStore store = new ObjectStorageArtifactStore(client, "artifacts/");

    @Test
    void storesUnderThePrefixAndResolvesByBareKey() {
        byte[] content = "shot".getBytes(StandardCharsets.UTF_8);

        store.store("run-1/chromium/shot.png", content);

        assertThat(store.exists("run-1/chromium/shot.png")).isTrue();
        assertThat(store.resolve("run-1/chromium/shot.png")).isEqualTo(content);
        // FI-ENT1-E: the underlying client key is namespaced under the prefix AND the current tenant
        // (no tenant bound here → the system tenant).
        assertThat(client.exists("artifacts/__system__/run-1/chromium/shot.png")).isTrue();
    }

    @Test
    void listStripsThePrefixBackToBareKeys() {
        store.store("run-1/a.png", new byte[]{1});
        store.store("run-1/b.png", new byte[]{2});

        assertThat(store.list("run-1/")).containsExactlyInAnyOrder("run-1/a.png", "run-1/b.png");
    }

    @Test
    void deleteRemovesTheObject() {
        store.store("k.txt", new byte[]{1});
        store.delete("k.txt");

        assertThat(store.exists("k.txt")).isFalse();
    }

    @Test
    void rejectsTraversalAndBlankKeys() {
        assertThatThrownBy(() -> store.store("../evil", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> store.store("", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
