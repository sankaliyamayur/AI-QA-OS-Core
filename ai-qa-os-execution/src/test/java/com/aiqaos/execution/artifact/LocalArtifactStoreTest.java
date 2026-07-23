package com.aiqaos.execution.artifact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** SCALE-1: local artifact store round-trip + key-traversal guard. */
class LocalArtifactStoreTest {

    @Test
    void storesAndResolvesByKey(@TempDir Path dir) {
        LocalArtifactStore store = new LocalArtifactStore(dir.toString());
        byte[] content = "screenshot-bytes".getBytes(StandardCharsets.UTF_8);

        String key = store.store("run-1/chromium/shot.png", content);

        assertThat(key).isEqualTo("run-1/chromium/shot.png");
        assertThat(store.exists("run-1/chromium/shot.png")).isTrue();
        assertThat(store.resolve("run-1/chromium/shot.png")).isEqualTo(content);
    }

    @Test
    void existsIsFalseForMissingKey(@TempDir Path dir) {
        assertThat(new LocalArtifactStore(dir.toString()).exists("nope.txt")).isFalse();
    }

    @Test
    void rejectsKeysThatEscapeTheBaseDir(@TempDir Path dir) {
        LocalArtifactStore store = new LocalArtifactStore(dir.toString());

        assertThatThrownBy(() -> store.store("../evil.txt", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> store.store("", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listsByPrefixAndDeletes(@TempDir Path dir) {
        LocalArtifactStore store = new LocalArtifactStore(dir.toString());
        store.store("run-1/a.txt", new byte[]{1});
        store.store("run-1/b.txt", new byte[]{2});
        store.store("run-2/c.txt", new byte[]{3});

        assertThat(store.list("run-1/")).containsExactlyInAnyOrder("run-1/a.txt", "run-1/b.txt");
        assertThat(store.list("")).hasSize(3);

        store.delete("run-1/a.txt");
        assertThat(store.exists("run-1/a.txt")).isFalse();
        assertThat(store.list("run-1/")).containsExactly("run-1/b.txt");
    }
}
