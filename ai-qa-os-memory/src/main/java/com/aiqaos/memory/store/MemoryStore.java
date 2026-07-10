package com.aiqaos.memory.store;

import java.util.Optional;
import java.time.Duration;

public interface MemoryStore {
    void put(String key, Object value, Duration ttl);
    Optional<Object> get(String key);
    void remove(String key);
    void clear();
}