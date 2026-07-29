package com.aiqaos.healing.memory;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.memory.store.MemoryStore;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * HEAL-4: cross-run AI healing memory. Remembers a validated locator heal keyed by the broken
 * locator, so when the same element drifts again — even inside a different test — the platform can
 * {@link #recall} the previously-validated locator instantly (the loop's "Future Auto Recovery"),
 * and flag a locator {@link #isKnownFragile fragile} once it has drifted more than once.
 *
 * <p>Reuses the {@code memory} {@link MemoryStore} (no new module). Keys are <b>tenant-scoped</b> via
 * ENT-1's {@link TenantContextHolder}, so one project's heals never recall under another's.
 */
@Component
public class HealingMemory {

    private static final Duration TTL = Duration.ofDays(90); // institutional knowledge — long-lived
    private static final String KEY_PREFIX = "healing:heal:";

    private final MemoryStore memoryStore;

    public HealingMemory(MemoryStore memoryStore) {
        this.memoryStore = memoryStore;
    }

    /**
     * Record a validated heal. If this broken locator was healed before, the element has drifted
     * again — its {@code reuseCount} increments and it is marked {@code fragile}.
     */
    public HealedLocatorRecord remember(String brokenLocator, String healedLocator, String strategy,
                                        double confidence) {
        String tenantId = currentTenantId();
        String key = key(tenantId, brokenLocator);

        Optional<HealedLocatorRecord> existing = read(key);
        int reuseCount = existing.map(r -> r.getReuseCount() + 1).orElse(0);
        boolean fragile = existing.isPresent(); // healed before → drifted again → fragile

        HealedLocatorRecord record = new HealedLocatorRecord(brokenLocator, healedLocator, strategy,
                confidence, reuseCount, fragile, tenantId, LocalDateTime.now());
        memoryStore.put(key, record, TTL);
        return record;
    }

    /** The previously-validated heal for {@code brokenLocator} in the current tenant, if any. */
    public Optional<HealedLocatorRecord> recall(String brokenLocator) {
        return read(key(currentTenantId(), brokenLocator));
    }

    /** True if this locator has drifted before and is therefore known to be fragile. */
    public boolean isKnownFragile(String brokenLocator) {
        return recall(brokenLocator).map(HealedLocatorRecord::isFragile).orElse(false);
    }

    private Optional<HealedLocatorRecord> read(String key) {
        Optional<Object> val = memoryStore.get(key);
        if (val.isPresent() && val.get() instanceof HealedLocatorRecord record) {
            return Optional.of(record);
        }
        return Optional.empty();
    }

    private String key(String tenantId, String brokenLocator) {
        return KEY_PREFIX + tenantId + ":" + brokenLocator;
    }

    private String currentTenantId() {
        return TenantContextHolder.current()
                .map(TenantContext::getTenantId)
                .orElse(TenantContext.SYSTEM_TENANT);
    }
}
