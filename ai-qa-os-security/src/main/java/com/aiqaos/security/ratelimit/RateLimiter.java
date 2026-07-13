package com.aiqaos.security.ratelimit;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiter — enforces per-user and per-agent request quotas.
 *
 * Future Phase 13 (API Gateway) will call this before routing to Security.
 *
 * Examples:
 *  - 100 requests / minute per user
 *  - Agent execution quotas (e.g. 10 workflow executions / hour)
 */
@Component
public class RateLimiter {

    // key: userId or agentId, value: request counter
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> windowStart    = new ConcurrentHashMap<>();

    private static final int DEFAULT_MAX_REQUESTS = 100;
    private static final int WINDOW_MINUTES       = 1;

    /**
     * Returns true if the request is within quota, false if rate-limited.
     */
    public boolean isAllowed(String principalId) {
        return isAllowed(principalId, DEFAULT_MAX_REQUESTS);
    }

    public boolean isAllowed(String principalId, int maxRequests) {
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime start = windowStart.computeIfAbsent(principalId, k -> now);

        // Reset window if expired
        if (now.isAfter(start.plusMinutes(WINDOW_MINUTES))) {
            windowStart.put(principalId, now);
            requestCounts.put(principalId, new AtomicInteger(0));
        }

        int count = requestCounts
                .computeIfAbsent(principalId, k -> new AtomicInteger(0))
                .incrementAndGet();

        return count <= maxRequests;
    }

    /**
     * Reset the counter for a given principal (e.g. after authentication).
     */
    public void reset(String principalId) {
        requestCounts.remove(principalId);
        windowStart.remove(principalId);
    }
}
