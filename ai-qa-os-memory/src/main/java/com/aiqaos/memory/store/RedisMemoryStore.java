package com.aiqaos.memory.store;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Optional;
import java.time.Duration;

/**
 * FI-ENT1-E (ADR-056): short-term Redis cache keys are namespaced by the active tenant ID (<tenant>:<key>).
 */
public class RedisMemoryStore implements MemoryStore {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void put(String key, Object value, Duration ttl) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(tenantKey(key), value, ttl);
            } catch (Exception e) {
                // Fallback
            }
        }
    }

    @Override
    public Optional<Object> get(String key) {
        if (redisTemplate != null) {
            try {
                return Optional.ofNullable(redisTemplate.opsForValue().get(tenantKey(key)));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public void remove(String key) {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(tenantKey(key));
            } catch (Exception e) {
                // Log
            }
        }
    }

    @Override
    public void clear() {
        // No-op or namespace flush
    }

    private String tenantKey(String key) {
        String tenant = TenantContextHolder.current()
                .map(TenantContext::getTenantId)
                .orElse(TenantContext.SYSTEM_TENANT);
        return tenant + ":" + key;
    }
}