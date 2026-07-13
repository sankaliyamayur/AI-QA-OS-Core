package com.aiqaos.security.cache;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Security Token Cache — in-memory JWT validation cache.
 *
 * Avoids repeated cryptographic signature verification on every request
 * for high-traffic scenarios.
 *
 * Future upgrade: replace backing store with Redis for distributed caching.
 */
@Component
public class SecurityTokenCache {

    private record CacheEntry(String userId, LocalDateTime expiresAt) {}

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Store a validated token result in the cache.
     *
     * @param token     raw JWT string
     * @param userId    extracted userId
     * @param expiresAt token expiry time
     */
    public void put(String token, String userId, LocalDateTime expiresAt) {
        cache.put(token, new CacheEntry(userId, expiresAt));
    }

    /**
     * Retrieve cached userId for a token, or null if not found / expired.
     */
    public String get(String token) {
        CacheEntry entry = cache.get(token);
        if (entry == null) return null;
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            cache.remove(token);   // evict expired entry
            return null;
        }
        return entry.userId();
    }

    /**
     * Explicitly invalidate a token (on logout or revocation).
     */
    public void invalidate(String token) {
        cache.remove(token);
    }

    /**
     * Returns true if the token is already cached and valid.
     */
    public boolean contains(String token) {
        return get(token) != null;
    }
}
