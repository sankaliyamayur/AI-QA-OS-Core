package com.aiqaos.memory.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.time.Duration;

@Component
public class RedisMemoryStore implements MemoryStore {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void put(String key, Object value, Duration ttl) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, ttl);
            } catch (Exception e) {
                // Fallback
            }
        }
    }

    @Override
    public Optional<Object> get(String key) {
        if (redisTemplate != null) {
            try {
                return Optional.ofNullable(redisTemplate.opsForValue().get(key));
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
                redisTemplate.delete(key);
            } catch (Exception e) {
                // Log
            }
        }
    }

    @Override
    public void clear() {
        // No-op or namespace flush
    }
}