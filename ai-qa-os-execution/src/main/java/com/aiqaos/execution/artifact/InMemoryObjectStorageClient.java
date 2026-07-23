package com.aiqaos.execution.artifact;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ENT-5 reference {@link ObjectStorageClient}: a map-backed store proving the object-storage
 * {@link ArtifactStore} wiring end-to-end. Active only when object storage is selected
 * ({@code aiqaos.artifacts.store=object}) and no real client bean is present — a real S3/GCS client
 * (a future bean) overrides it via {@link ConditionalOnMissingBean}. Not durable — for tests/local.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.artifacts.store", havingValue = "object")
@ConditionalOnMissingBean(ObjectStorageClient.class)
public class InMemoryObjectStorageClient implements ObjectStorageClient {

    private record Entry(byte[] content, Instant lastModified) {
    }

    private final Map<String, Entry> objects = new ConcurrentHashMap<>();

    @Override
    public void put(String key, byte[] content) {
        objects.put(key, new Entry(content.clone(), Instant.now()));
    }

    @Override
    public byte[] get(String key) {
        Entry entry = objects.get(key);
        if (entry == null) {
            throw new NoSuchElementException("No object: " + key);
        }
        return entry.content().clone();
    }

    @Override
    public boolean exists(String key) {
        return objects.containsKey(key);
    }

    @Override
    public List<String> list(String prefix) {
        List<String> keys = new ArrayList<>();
        for (String key : objects.keySet()) {
            if (prefix == null || prefix.isEmpty() || key.startsWith(prefix)) {
                keys.add(key);
            }
        }
        return keys;
    }

    @Override
    public void delete(String key) {
        objects.remove(key);
    }

    @Override
    public Instant lastModified(String key) {
        Entry entry = objects.get(key);
        if (entry == null) {
            throw new NoSuchElementException("No object: " + key);
        }
        return entry.lastModified();
    }

    /** Test hook: force an object's timestamp (real object storage carries its own last-modified). */
    void setLastModified(String key, Instant when) {
        Entry entry = objects.get(key);
        if (entry != null) {
            objects.put(key, new Entry(entry.content(), when));
        }
    }
}
