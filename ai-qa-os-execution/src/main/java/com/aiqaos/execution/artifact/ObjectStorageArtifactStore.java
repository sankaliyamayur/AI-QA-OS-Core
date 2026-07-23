package com.aiqaos.execution.artifact;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ENT-5: an {@link ArtifactStore} backed by an {@link ObjectStorageClient}. This is the code-level
 * SCALE-1 unblock — artifacts are addressed by key in shared storage rather than a host path, so a
 * cross-host worker's artifacts are reachable from anywhere. Keys are namespaced under a configurable
 * prefix. Active when {@code aiqaos.artifacts.store=object}; validated against the in-memory client.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.artifacts.store", havingValue = "object")
public class ObjectStorageArtifactStore implements ArtifactStore {

    private final ObjectStorageClient client;
    private final String prefix;

    public ObjectStorageArtifactStore(ObjectStorageClient client,
                                      @Value("${aiqaos.artifacts.object.prefix:artifacts/}") String prefix) {
        this.client = client;
        this.prefix = (prefix == null) ? "" : prefix;
    }

    @Override
    public String store(String key, byte[] content) {
        client.put(fullKey(key), content);
        return key;
    }

    @Override
    public byte[] resolve(String key) {
        return client.get(fullKey(key));
    }

    @Override
    public boolean exists(String key) {
        return client.exists(fullKey(key));
    }

    @Override
    public List<String> list(String keyPrefix) {
        String full = prefix + (keyPrefix == null ? "" : keyPrefix);
        return client.list(full).stream()
                .map(k -> k.startsWith(prefix) ? k.substring(prefix.length()) : k)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String key) {
        client.delete(fullKey(key));
    }

    @Override
    public Instant lastModified(String key) {
        return client.lastModified(fullKey(key));
    }

    private String fullKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Artifact key must not be blank");
        }
        if (key.contains("..")) {
            throw new IllegalArgumentException("Illegal artifact key: " + key);
        }
        return prefix + key;
    }
}
