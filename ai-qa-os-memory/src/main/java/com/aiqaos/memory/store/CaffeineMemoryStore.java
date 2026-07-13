package com.aiqaos.memory.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Optional;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CaffeineMemoryStore implements MemoryStore {
    private final Cache<String, Object> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10000)
            .build();

    @Override
    public void put(String key, Object value, Duration ttl) {
        cache.put(key, value);
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public void remove(String key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }
}