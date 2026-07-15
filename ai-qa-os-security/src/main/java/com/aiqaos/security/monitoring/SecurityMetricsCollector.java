package com.aiqaos.security.monitoring;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

@Component("securityModuleMetricsCollector")
public class SecurityMetricsCollector {

    private final AtomicLong failedLogins = new AtomicLong(0);
    private final AtomicLong blockedTokens = new AtomicLong(0);
    private final AtomicLong expiredTokens = new AtomicLong(0);
    private final AtomicLong rateLimitHits = new AtomicLong(0);

    public void incrementFailedLogins() { failedLogins.incrementAndGet(); }
    public void incrementBlockedTokens() { blockedTokens.incrementAndGet(); }
    public void incrementExpiredTokens() { expiredTokens.incrementAndGet(); }
    public void incrementRateLimitHits() { rateLimitHits.incrementAndGet(); }

    public long getFailedLogins() { return failedLogins.get(); }
    public long getBlockedTokens() { return blockedTokens.get(); }
    public long getExpiredTokens() { return expiredTokens.get(); }
    public long getRateLimitHits() { return rateLimitHits.get(); }
}
