package com.aiqaos.execution.artifact;

import java.time.Instant;
import java.util.List;

/**
 * ENT-5: the cloud-storage seam that {@link ObjectStorageArtifactStore} sits on. The in-memory
 * {@link InMemoryObjectStorageClient} is the validatable reference; a real S3/GCS adapter (AWS/GCS
 * SDK + credentials) is a thin deferred binding — not built in this environment (no bucket/creds).
 */
public interface ObjectStorageClient {

    void put(String key, byte[] content);

    byte[] get(String key);

    boolean exists(String key);

    List<String> list(String prefix);

    void delete(String key);

    Instant lastModified(String key);
}
