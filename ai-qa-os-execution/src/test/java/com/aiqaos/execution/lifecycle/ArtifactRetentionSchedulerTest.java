package com.aiqaos.execution.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.execution.artifact.ArtifactStore;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * ENT-5 (FI-ENT5-F): the scheduled sweep must bind each tenant, because the artifact stores are
 * tenant-namespaced (ADR-056) — an unbound sweep would purge {@code __system__} only and silently
 * leak every real tenant's artifacts. The fake store below reproduces that namespacing faithfully,
 * so these assertions fail if the scheduler ever stops binding.
 */
class ArtifactRetentionSchedulerTest {

    @AfterEach
    void clearTenant() {
        TenantContextHolder.clear();
    }

    @Test
    void sweepsEveryTenantSuppliedBySource() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("__system__", "old-sys", 100);
        store.seed("acme", "old-acme", 100);
        store.seed("globex", "old-globex", 100);

        ArtifactRetentionScheduler scheduler = scheduler(store,
                () -> List.of("__system__", "acme", "globex"));

        int purged = scheduler.purgeAllTenants();

        assertThat(purged).isEqualTo(3);
        assertThat(store.sweptTenants).containsExactly("__system__", "acme", "globex");
        assertThat(store.keysFor("acme")).isEmpty();
        assertThat(store.keysFor("globex")).isEmpty();
    }

    @Test
    void keepsFreshArtifactsWithinEachTenant() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("acme", "old", 100);
        store.seed("acme", "fresh", 1);

        int purged = scheduler(store, () -> List.of("acme")).purgeAllTenants();

        assertThat(purged).isEqualTo(1);
        assertThat(store.keysFor("acme")).containsExactly("fresh");
    }

    @Test
    void withoutATenantSourceSweepsTheSystemTenantOnly() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("__system__", "old-sys", 100);
        store.seed("acme", "old-acme", 100);

        ArtifactRetentionScheduler scheduler = new ArtifactRetentionScheduler(
                new ArtifactRetentionService(store, true, 30, ""), noTenantSource(),
                ArtifactRetentionScheduler.CRON_DISABLED);

        int purged = scheduler.purgeAllTenants();

        assertThat(purged).isEqualTo(1);
        assertThat(store.sweptTenants).containsExactly("__system__");
        assertThat(store.keysFor("acme")).containsExactly("old-acme"); // untouched, not fabricated
    }

    @Test
    void oneTenantsFailureDoesNotAbortTheSweep() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.failFor("broken");
        store.seed("acme", "old-acme", 100);
        store.seed("globex", "old-globex", 100);

        int purged = scheduler(store, () -> List.of("broken", "acme", "globex")).purgeAllTenants();

        assertThat(purged).isEqualTo(2); // the two healthy tenants still swept
        assertThat(store.keysFor("acme")).isEmpty();
        assertThat(store.keysFor("globex")).isEmpty();
    }

    @Test
    void duplicateAndBlankTenantIdsAreNormalised() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("acme", "old", 100);

        int purged = scheduler(store, () -> {
            List<String> ids = new ArrayList<>();
            ids.add("acme");
            ids.add("acme"); // duplicate — must not double-sweep
            ids.add("  ");   // blank — must be ignored
            ids.add(null);
            return ids;
        }).purgeAllTenants();

        assertThat(purged).isEqualTo(1);
        assertThat(store.sweptTenants).containsExactly("acme");
    }

    @Test
    void emptyTenantListFallsBackToTheSystemTenant() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("__system__", "old", 100);

        assertThat(scheduler(store, List::of).purgeAllTenants()).isEqualTo(1);
        assertThat(store.sweptTenants).containsExactly("__system__");
    }

    @Test
    void leavesTheSchedulerThreadUnbound() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("acme", "old", 100);

        scheduler(store, () -> List.of("acme")).purgeAllTenants();

        assertThat(TenantContextHolder.current()).isEmpty();
    }

    @Test
    void restoresAnAlreadyBoundContextAfterTheSweep() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("acme", "old", 100);
        TenantContext caller = TenantContext.ofTenant("caller");
        TenantContextHolder.set(caller);

        scheduler(store, () -> List.of("acme")).purgeAllTenants();

        assertThat(TenantContextHolder.current()).contains(caller);
    }

    @Test
    void disabledRetentionPurgesNothingEvenWhenScheduled() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("acme", "old", 100);

        ArtifactRetentionScheduler scheduler = new ArtifactRetentionScheduler(
                new ArtifactRetentionService(store, false, 30, ""),
                objectProvider(() -> List.of("acme")), ArtifactRetentionScheduler.CRON_DISABLED);

        assertThat(scheduler.purgeAllTenants()).isZero();
        assertThat(store.keysFor("acme")).containsExactly("old");
    }

    @Test
    void scheduledEntryPointNeverThrows() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        ArtifactRetentionScheduler scheduler = scheduler(store, () -> {
            throw new IllegalStateException("registry down");
        });

        // an escaping exception would silently suppress all future runs of the trigger
        assertThatCode(scheduler::runScheduledPurge).doesNotThrowAnyException();
        assertThat(TenantContextHolder.current()).isEmpty();
    }

    // --- timer lifecycle -------------------------------------------------------------------------

    @Test
    void armedTimerActuallyRunsTheSweep() throws Exception {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("acme", "old", 100);
        // every second — proves the owned timer really fires without depending on @EnableScheduling
        ArtifactRetentionScheduler scheduler = scheduler(store, () -> List.of("acme"), "* * * * * *");

        scheduler.start();
        try {
            long deadline = System.currentTimeMillis() + 5_000;
            while (System.currentTimeMillis() < deadline && !store.keysFor("acme").isEmpty()) {
                Thread.sleep(50);
            }
        } finally {
            scheduler.stop();
        }

        assertThat(store.keysFor("acme")).as("timer-driven sweep should have purged the aged artifact").isEmpty();
    }

    @Test
    void cronDisabledStartsNoTimer() {
        TenantScopedFakeStore store = new TenantScopedFakeStore();
        store.seed("acme", "old", 100);
        ArtifactRetentionScheduler scheduler =
                scheduler(store, () -> List.of("acme"), ArtifactRetentionScheduler.CRON_DISABLED);

        scheduler.start();
        scheduler.stop(); // must be safe with no timer ever created

        assertThat(store.sweptTenants).isEmpty(); // on-demand only — nothing ran
    }

    @Test
    void malformedCronFailsFastRatherThanNeverRunning() {
        ArtifactRetentionScheduler scheduler =
                scheduler(new TenantScopedFakeStore(), List::of, "not a cron");

        assertThatThrownBy(scheduler::start).isInstanceOf(IllegalArgumentException.class);
    }

    // --- helpers ---------------------------------------------------------------------------------

    private static ArtifactRetentionScheduler scheduler(ArtifactStore store, RetentionTenantSource source) {
        return scheduler(store, source, ArtifactRetentionScheduler.CRON_DISABLED);
    }

    private static ArtifactRetentionScheduler scheduler(ArtifactStore store, RetentionTenantSource source,
                                                        String cron) {
        return new ArtifactRetentionScheduler(
                new ArtifactRetentionService(store, true, 30, ""), objectProvider(source), cron);
    }

    private static ObjectProvider<RetentionTenantSource> noTenantSource() {
        return objectProvider(null);
    }

    private static <T> ObjectProvider<T> objectProvider(T instance) {
        return new ObjectProvider<>() {
            @Override public T getObject() { return instance; }
            @Override public T getObject(Object... args) { return instance; }
            @Override public T getIfAvailable() { return instance; }
            @Override public T getIfUnique() { return instance; }
        };
    }

    /**
     * Mirrors {@code ObjectStorageArtifactStore}'s tenant namespacing: every operation resolves
     * against the tenant bound to the calling thread, falling back to the system tenant when unbound.
     */
    private static final class TenantScopedFakeStore implements ArtifactStore {

        private final Map<String, Map<String, Instant>> byTenant = new LinkedHashMap<>();
        private final List<String> sweptTenants = new ArrayList<>();
        private String failingTenant;

        void seed(String tenant, String key, int ageDays) {
            byTenant.computeIfAbsent(tenant, t -> new LinkedHashMap<>())
                    .put(key, Instant.now().minus(Duration.ofDays(ageDays)));
        }

        void failFor(String tenant) {
            this.failingTenant = tenant;
        }

        List<String> keysFor(String tenant) {
            return new ArrayList<>(byTenant.getOrDefault(tenant, Map.of()).keySet());
        }

        private static String currentTenant() {
            return TenantContextHolder.current()
                    .map(TenantContext::getTenantId)
                    .orElse(TenantContext.SYSTEM_TENANT);
        }

        private Map<String, Instant> scope() {
            return byTenant.computeIfAbsent(currentTenant(), t -> new LinkedHashMap<>());
        }

        @Override
        public List<String> list(String prefix) {
            String tenant = currentTenant();
            sweptTenants.add(tenant);
            if (tenant.equals(failingTenant)) {
                throw new IllegalStateException("storage unavailable for " + tenant);
            }
            return new ArrayList<>(scope().keySet());
        }

        @Override
        public Instant lastModified(String key) {
            Instant at = scope().get(key);
            if (at == null) {
                throw new NoSuchElementException(key);
            }
            return at;
        }

        @Override public void delete(String key) { scope().remove(key); }
        @Override public String store(String key, byte[] content) { scope().put(key, Instant.now()); return key; }
        @Override public boolean exists(String key) { return scope().containsKey(key); }

        @Override
        public byte[] resolve(String key) {
            if (!exists(key)) {
                throw new NoSuchElementException(key);
            }
            return new byte[0];
        }
    }
}
