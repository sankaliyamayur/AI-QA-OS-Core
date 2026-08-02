package com.aiqaos.execution.artifact;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * FI-ENT5-A (ADR-071): the uploader reads produced artifact files and stores their bytes in the
 * durable {@link ArtifactStore} under deterministic keys. Mockito-free — a fake in-memory ArtifactStore
 * + a real {@code @TempDir}. No browser/bucket needed; the live round-trip is user-run.
 */
class ArtifactUploaderTest {

    private static final UUID EXEC = UUID.fromString("00000000-0000-0000-0000-0000000000ab");

    @Test
    void uploadsExistingFiles_underDeterministicKeys_skipsNullAndMissing(@TempDir Path dir) throws Exception {
        Path shot = Files.write(dir.resolve("shot.png"), "IMG".getBytes(StandardCharsets.UTF_8));
        Path vid = Files.write(dir.resolve("vid.webm"), "VID".getBytes(StandardCharsets.UTF_8));
        FakeArtifactStore store = new FakeArtifactStore();
        ArtifactUploader uploader = new ArtifactUploader(store, signer(false, ""));

        ArtifactUploadRequest req = new ArtifactUploadRequest(
                EXEC, "TC-AL-003", "chromium", 2,
                shot.toString(),                       // exists
                vid.toString(),                        // exists
                null,                                  // trace: null -> skipped
                dir.resolve("missing.log").toString(), // log: no file -> skipped
                null);                                 // report: null -> skipped

        List<String> keys = uploader.upload(req);

        assertEquals(2, keys.size(), "only the two existing files are stored");
        String shotKey = "executions/" + EXEC + "/run-2/chromium/TC-AL-003/screenshot";
        String videoKey = "executions/" + EXEC + "/run-2/chromium/TC-AL-003/video";
        assertTrue(store.objects.containsKey(shotKey));
        assertArrayEquals("IMG".getBytes(StandardCharsets.UTF_8), store.objects.get(shotKey));
        assertArrayEquals("VID".getBytes(StandardCharsets.UTF_8), store.objects.get(videoKey));
        assertFalse(store.objects.containsKey("executions/" + EXEC + "/run-2/chromium/TC-AL-003/trace"));
    }

    @Test
    void keyFor_isDeterministicAndNullSafe() {
        ArtifactUploadRequest req = new ArtifactUploadRequest(EXEC, null, null, 1, null, null, null, null, null);
        assertEquals("executions/" + EXEC + "/run-1/unknown/unknown/log",
                ArtifactUploader.keyFor(req, "log"), "null browser/testCaseId fall back to 'unknown'");
    }

    @Test
    void storeFailureIsSwallowed_bestEffort(@TempDir Path dir) throws Exception {
        Path shot = Files.write(dir.resolve("shot.png"), "IMG".getBytes(StandardCharsets.UTF_8));
        ArtifactUploader uploader = new ArtifactUploader(new ThrowingArtifactStore(), signer(false, ""));

        List<String> keys = uploader.upload(new ArtifactUploadRequest(
                EXEC, "TC-1", "firefox", 1, shot.toString(), null, null, null, null));

        assertTrue(keys.isEmpty(), "a throwing store must not fail the upload — the key is simply not recorded");
    }

    @Test
    void signingEnabled_writesVerifiableSignatureSidecar(@TempDir Path dir) throws Exception {
        Path shot = Files.write(dir.resolve("shot.png"), "IMG".getBytes(StandardCharsets.UTF_8));
        FakeArtifactStore store = new FakeArtifactStore();
        ArtifactSigner signer = signer(true, "sec6-key");
        ArtifactUploader uploader = new ArtifactUploader(store, signer);

        uploader.upload(new ArtifactUploadRequest(
                EXEC, "TC-AL-003", "chromium", 2, shot.toString(), null, null, null, null));

        String artifactKey = "executions/" + EXEC + "/run-2/chromium/TC-AL-003/screenshot";
        String sigKey = artifactKey + ".sig";
        assertTrue(store.objects.containsKey(sigKey), "a .sig sidecar is written alongside the artifact");
        String sig = new String(store.objects.get(sigKey), StandardCharsets.UTF_8);
        assertTrue(signer.verify("IMG".getBytes(StandardCharsets.UTF_8), sig), "the sidecar verifies the stored bytes");
    }

    private static ArtifactSigner signer(boolean enabled, String secret) {
        ArtifactSignatureProperties p = new ArtifactSignatureProperties();
        p.setEnabled(enabled);
        p.setSecret(secret);
        return new ArtifactSigner(p);
    }

    // --- fakes -----------------------------------------------------------------------------------

    private static final class FakeArtifactStore implements ArtifactStore {
        final Map<String, byte[]> objects = new HashMap<>();

        @Override public String store(String key, byte[] content) { objects.put(key, content); return key; }
        @Override public byte[] resolve(String key) { return objects.get(key); }
        @Override public boolean exists(String key) { return objects.containsKey(key); }
        @Override public List<String> list(String prefix) { return new ArrayList<>(objects.keySet()); }
        @Override public void delete(String key) { objects.remove(key); }
        @Override public Instant lastModified(String key) { return Instant.EPOCH; }
    }

    private static final class ThrowingArtifactStore implements ArtifactStore {
        @Override public String store(String key, byte[] content) { throw new RuntimeException("boom"); }
        @Override public byte[] resolve(String key) { return null; }
        @Override public boolean exists(String key) { return false; }
        @Override public List<String> list(String prefix) { return List.of(); }
        @Override public void delete(String key) { }
        @Override public Instant lastModified(String key) { return Instant.EPOCH; }
    }
}
