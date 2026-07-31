package com.aiqaos.execution.artifact;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * ENT-5 (ADR-068): proves the {@link S3ObjectStorageClient} maps the {@link ObjectStorageClient} seam
 * onto S3 requests/responses/exceptions correctly. Mockito-free — the {@link S3Client} is a hand-backed
 * JDK dynamic proxy over an in-memory bucket. The live MinIO/S3 round-trip is user-run (needs the
 * container + creds). The optional {@code s3} dep is on this module's own test classpath.
 */
class S3ObjectStorageClientTest {

    private static final String BUCKET = "aiqaos-artifacts";

    private final Map<String, byte[]> store = new HashMap<>();
    private final Map<String, Instant> modified = new HashMap<>();
    private S3ObjectStorageClient client;

    @BeforeEach
    void setUp() {
        store.clear();
        modified.clear();
        S3StorageProperties props = new S3StorageProperties();
        props.setBucket(BUCKET);
        client = new S3ObjectStorageClient(fakeS3(), props);
    }

    @Test
    void put_then_get_roundTrips() {
        client.put("a/b.txt", "hello".getBytes(StandardCharsets.UTF_8));
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), client.get("a/b.txt"));
    }

    @Test
    void get_missingKey_throwsNoSuchElement() {
        assertThrows(NoSuchElementException.class, () -> client.get("nope"));
    }

    @Test
    void exists_reflectsPresence() {
        client.put("k", new byte[]{1});
        assertTrue(client.exists("k"));
        assertFalse(client.exists("absent"));
    }

    @Test
    void list_filtersByPrefix() {
        client.put("artifacts/t1/a", new byte[]{1});
        client.put("artifacts/t1/b", new byte[]{2});
        client.put("artifacts/t2/c", new byte[]{3});
        List<String> keys = client.list("artifacts/t1/");
        assertEquals(2, keys.size());
        assertTrue(keys.contains("artifacts/t1/a"));
        assertTrue(keys.contains("artifacts/t1/b"));
    }

    @Test
    void delete_removes() {
        client.put("k", new byte[]{1});
        client.delete("k");
        assertFalse(client.exists("k"));
    }

    @Test
    void lastModified_returnsHeadTimestamp_andThrowsWhenMissing() {
        Instant when = Instant.parse("2026-07-31T10:15:30Z");
        client.put("k", new byte[]{1});
        modified.put("k", when);
        assertEquals(when, client.lastModified("k"));
        assertThrows(NoSuchElementException.class, () -> client.lastModified("absent"));
    }

    /** A JDK-proxy {@link S3Client} backed by the in-memory maps — implements only the methods used. */
    @SuppressWarnings("unchecked")
    private S3Client fakeS3() {
        return (S3Client) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{S3Client.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "putObject": {
                            String key = ((PutObjectRequest) args[0]).key();
                            store.put(key, readBytes((RequestBody) args[1]));
                            modified.put(key, Instant.now());
                            return null;
                        }
                        case "getObjectAsBytes": {
                            String key = ((GetObjectRequest) args[0]).key();
                            byte[] content = store.get(key);
                            if (content == null) {
                                throw NoSuchKeyException.builder().message("no key: " + key).build();
                            }
                            return ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content);
                        }
                        case "headObject": {
                            String key = ((HeadObjectRequest) args[0]).key();
                            if (!store.containsKey(key)) {
                                throw NoSuchKeyException.builder().message("no key: " + key).build();
                            }
                            return HeadObjectResponse.builder()
                                    .lastModified(modified.getOrDefault(key, Instant.EPOCH)).build();
                        }
                        case "listObjectsV2": {
                            String prefix = ((ListObjectsV2Request) args[0]).prefix();
                            List<S3Object> contents = new ArrayList<>();
                            for (String key : store.keySet()) {
                                if (prefix == null || prefix.isEmpty() || key.startsWith(prefix)) {
                                    contents.add(S3Object.builder().key(key).build());
                                }
                            }
                            return ListObjectsV2Response.builder().contents(contents).isTruncated(false).build();
                        }
                        case "deleteObject": {
                            DeleteObjectRequest.Builder b = DeleteObjectRequest.builder();
                            ((Consumer<DeleteObjectRequest.Builder>) args[0]).accept(b);
                            String key = b.build().key();
                            store.remove(key);
                            modified.remove(key);
                            return null;
                        }
                        default:
                            return null;
                    }
                });
    }

    private static byte[] readBytes(RequestBody body) {
        try {
            return body.contentStreamProvider().newStream().readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
