package com.aiqaos.dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        return new ArtifactController(null, provider(store), "./playwright-output");
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
        ResponseEntity<Resource> res = new ArtifactController(null, provider(null), "./playwright-output")
                .serveFromStore(get("/api/artifacts/store/" + KEY));
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
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

    private static ObjectProvider<ArtifactStore> provider(ArtifactStore store) {
        return new ObjectProvider<>() {
            @Override public ArtifactStore getObject() { return store; }
            @Override public ArtifactStore getObject(Object... args) { return store; }
            @Override public ArtifactStore getIfAvailable() { return store; }
            @Override public ArtifactStore getIfUnique() { return store; }
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
