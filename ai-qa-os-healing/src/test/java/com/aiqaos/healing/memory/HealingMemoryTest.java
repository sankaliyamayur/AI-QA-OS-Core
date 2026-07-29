package com.aiqaos.healing.memory;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.memory.store.MemoryStore;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** HEAL-4: unit tests for the tenant-scoped cross-run healing memory. No Mockito. */
class HealingMemoryTest {

    /** Map-backed MemoryStore double. */
    private static final class MapMemoryStore implements MemoryStore {
        final Map<String, Object> m = new HashMap<>();
        @Override public void put(String k, Object v, Duration ttl) { m.put(k, v); }
        @Override public Optional<Object> get(String k) { return Optional.ofNullable(m.get(k)); }
        @Override public void remove(String k) { m.remove(k); }
        @Override public void clear() { m.clear(); }
    }

    private final HealingMemory memory = new HealingMemory(new MapMemoryStore());

    @AfterEach
    void tidy() {
        TenantContextHolder.clear();
    }

    @Test
    void remembersAndRecallsAValidatedHeal() {
        memory.remember("#oldId", "[data-testid=\"submit\"]", "TEST_ID", 0.95);
        Optional<HealedLocatorRecord> recalled = memory.recall("#oldId");
        assertThat(recalled).isPresent();
        assertThat(recalled.get().getHealedLocator()).isEqualTo("[data-testid=\"submit\"]");
        assertThat(recalled.get().getStrategy()).isEqualTo("TEST_ID");
    }

    @Test
    void recallOfUnknownLocatorIsEmpty() {
        assertThat(memory.recall("#neverSeen")).isEmpty();
    }

    @Test
    void firstHealIsNotYetFragile() {
        memory.remember("#btn", "#newBtn", "ID", 0.9);
        assertThat(memory.isKnownFragile("#btn")).isFalse();
        assertThat(memory.recall("#btn").get().getReuseCount()).isZero();
    }

    @Test
    void reDriftMarksFragileAndIncrementsReuse() {
        memory.remember("#btn", "#newBtn", "ID", 0.9);       // first heal
        HealedLocatorRecord second = memory.remember("#btn", "#newerBtn", "ID", 0.9); // drifted again
        assertThat(second.getReuseCount()).isEqualTo(1);
        assertThat(second.isFragile()).isTrue();
        assertThat(memory.isKnownFragile("#btn")).isTrue();
    }

    @Test
    void healsAreTenantIsolated() {
        TenantContextHolder.run(TenantContext.ofTenant("tenantA"),
                () -> memory.remember("#shared", "#healedA", "ID", 0.9));

        // A different tenant must not see tenant A's heal...
        TenantContextHolder.run(TenantContext.ofTenant("tenantB"),
                () -> assertThat(memory.recall("#shared")).isEmpty());
        // ...but tenant A still does.
        TenantContextHolder.run(TenantContext.ofTenant("tenantA"),
                () -> assertThat(memory.recall("#shared")).isPresent());
    }
}
