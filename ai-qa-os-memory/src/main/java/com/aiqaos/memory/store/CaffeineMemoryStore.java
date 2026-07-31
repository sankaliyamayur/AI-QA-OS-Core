package com.aiqaos.memory.store;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Optional;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * FI-ENT1-E (ADR-056): short-term Caffeine cache keys are namespaced by the active tenant ID (<tenant>:<key>).
 */
public class CaffeineMemoryStore implements MemoryStore {
    private final Cache<String, Object> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10000)
            .build();

    @Override
    public void put(String key, Object value, Duration ttl) {
        cache.put(tenantKey(key), value);
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(tenantKey(key)));
    }

    @Override
    public void remove(String key) {
        cache.invalidate(tenantKey(key));
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    private String tenantKey(String key) {
        String tenant = TenantContextHolder.current()
                .map(TenantContext::getTenantId)
                .orElse(TenantContext.SYSTEM_TENANT);
        return tenant + ":" + key;
    }
}