package com.aiqaos.execution.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.execution.artifact.LocalArtifactStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** ENT-5: age-based retention over a real (local) artifact store. */
class ArtifactRetentionServiceTest {

    private static void ageArtifact(Path dir, String key, int days) throws Exception {
        Files.setLastModifiedTime(dir.resolve(key), FileTime.from(Instant.now().minus(Duration.ofDays(days))));
    }

    @Test
    void purgesArtifactsOlderThanTheWindowAndKeepsFreshOnes(@TempDir Path dir) throws Exception {
        LocalArtifactStore store = new LocalArtifactStore(dir.toString());
        store.store("old.txt", new byte[]{1});
        store.store("fresh.txt", new byte[]{2});
        ageArtifact(dir, "old.txt", 100);

        ArtifactRetentionService service = new ArtifactRetentionService(store, true, 30, "");
        int deleted = service.purgeExpired();

        assertThat(deleted).isEqualTo(1);
        assertThat(store.exists("old.txt")).isFalse();
        assertThat(store.exists("fresh.txt")).isTrue();
    }

    @Test
    void disabledRetentionDeletesNothing(@TempDir Path dir) throws Exception {
        LocalArtifactStore store = new LocalArtifactStore(dir.toString());
        store.store("old.txt", new byte[]{1});
        ageArtifact(dir, "old.txt", 100);

        ArtifactRetentionService service = new ArtifactRetentionService(store, false, 30, "");

        assertThat(service.purgeExpired()).isZero();
        assertThat(store.exists("old.txt")).isTrue();
    }
}
