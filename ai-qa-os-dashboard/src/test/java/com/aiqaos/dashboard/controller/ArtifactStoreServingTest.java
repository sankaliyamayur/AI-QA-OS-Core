package com.aiqaos.dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aiqaos.execution.artifact.ArtifactSignatureProperties;
import com.aiqaos.execution.artifact.ArtifactSigner;
import com.aiqaos.execution.artifact.ArtifactStore;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * FI-ENT5-C (ADR-073): the durable serve-by-key endpoint resolves artifact bytes from ArtifactStore.
 * Mockito-free — a fake in-memory ArtifactStore + MockHttpServletRequest. No DB/context needed
 * (serveFromStore uses neither JdbcTemplate nor the base URL).
 */
class ArtifactStoreServingTest {

    private static final String KEY = "executions/abc/run-2/chromium/TC-AL-003/screenshot";

    private ArtifactController controller(ArtifactStore store) {
        return controller(store, signer(false, ""));
    }

    private ArtifactController controller(ArtifactStore store, ArtifactSigner signer) {
        return new ArtifactController(null, objectProvider(store), objectProvider(signer), "./playwright-output");
    }

    private static ArtifactSigner signer(boolean enabled, String secret) {
        ArtifactSignatureProperties p = new ArtifactSignatureProperties();
        p.setEnabled(enabled);
        p.setSecret(secret);
        return new ArtifactSigner(p);
    }

    private static MockHttpServletRequest get(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI(uri);
        return req;
    }

    @Test
    void servesStoredBytes_withTypeDerivedContentType() throws Exception {
        FakeArtifactStore store = new FakeArtifactStore();
        store.store(KEY, "IMG".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<Resource> res = controller(store).serveFromStore(get("/api/artifacts/store/" + KEY));

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("image/png", res.getHeaders().getContentType().toString());
        assertArrayEquals("IMG".getBytes(StandardCharsets.UTF_8),
                res.getBody().getInputStream().readAllBytes());
    }

    @Test
    void absentKey_is404() {
        ResponseEntity<Resource> res = controller(new FakeArtifactStore())
                .serveFromStore(get("/api/artifacts/store/" + KEY));
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    @Test
    void traversalKey_is400() {
        ResponseEntity<Resource> res = controller(new FakeArtifactStore())
                .serveFromStore(get("/api/artifacts/store/executions/../secret"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void noStore_is404() {
        ResponseEntity<Resource> res = new ArtifactController(
                null, objectProvider((ArtifactStore) null), objectProvider(signer(false, "")), "./playwright-output")
                .serveFromStore(get("/api/artifacts/store/" + KEY));
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    @Test
    void servedArtifact_integrityHeader_verifiedAndMismatch() {
        ArtifactSigner signer = signer(true, "sec6-key");
        FakeArtifactStore store = new FakeArtifactStore();
        byte[] bytes = "IMG".getBytes(StandardCharsets.UTF_8);
        store.store(KEY, bytes);

        // valid sidecar -> verified
        store.store(KEY + ".sig", signer.sign(bytes).getBytes(StandardCharsets.UTF_8));
        ResponseEntity<Resource> ok = controller(store, signer).serveFromStore(get("/api/artifacts/store/" + KEY));
        assertEquals("verified", ok.getHeaders().getFirst("X-Artifact-Integrity"));

        // tampered sidecar -> MISMATCH (still served — detection, not denial)
        store.store(KEY + ".sig", signer.sign("OTHER".getBytes(StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8));
        ResponseEntity<Resource> bad = controller(store, signer).serveFromStore(get("/api/artifacts/store/" + KEY));
        assertEquals(HttpStatus.OK, bad.getStatusCode());
        assertEquals("MISMATCH", bad.getHeaders().getFirst("X-Artifact-Integrity"));
    }

    @Test
    void servedArtifact_unsignedHeader_whenSigningOff() {
        FakeArtifactStore store = new FakeArtifactStore();
        store.store(KEY, "IMG".getBytes(StandardCharsets.UTF_8));
        ResponseEntity<Resource> res = controller(store).serveFromStore(get("/api/artifacts/store/" + KEY));
        assertEquals("unsigned", res.getHeaders().getFirst("X-Artifact-Integrity"));
    }

    @Test
    void contentTypeForKey_mapsTrailingType() {
        assertEquals("image/png", ArtifactController.contentTypeForKey("a/b/screenshot"));
        assertEquals("video/webm", ArtifactController.contentTypeForKey("a/b/video"));
        assertEquals("application/zip", ArtifactController.contentTypeForKey("a/b/trace"));
        assertEquals("text/html", ArtifactController.contentTypeForKey("a/b/report"));
        assertEquals("text/plain", ArtifactController.contentTypeForKey("a/b/log"));
        assertEquals("application/octet-stream", ArtifactController.contentTypeForKey("a/b/other"));
    }

    // --- fakes -----------------------------------------------------------------------------------

    private static <T> ObjectProvider<T> objectProvider(T instance) {
        return new ObjectProvider<>() {
            @Override public T getObject() { return instance; }
            @Override public T getObject(Object... args) { return instance; }
            @Override public T getIfAvailable() { return instance; }
            @Override public T getIfUnique() { return instance; }
        };
    }

    private static final class FakeArtifactStore implements ArtifactStore {
        private final Map<String, byte[]> objects = new HashMap<>();

        @Override public String store(String key, byte[] content) { objects.put(key, content); return key; }
        @Override public byte[] resolve(String key) {
            byte[] b = objects.get(key);
            if (b == null) {
                throw new NoSuchElementException(key); // mirrors Local/Object store on a missing key
            }
            return b;
        }
        @Override public boolean exists(String key) { return objects.containsKey(key); }
        @Override public List<String> list(String prefix) { return List.of(); }
        @Override public void delete(String key) { objects.remove(key); }
        @Override public Instant lastModified(String key) { return Instant.EPOCH; }
    }
}
