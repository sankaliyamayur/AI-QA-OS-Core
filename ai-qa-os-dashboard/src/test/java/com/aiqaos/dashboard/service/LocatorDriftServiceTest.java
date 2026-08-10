package com.aiqaos.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.dashboard.dto.LocatorDriftEntry;
import com.aiqaos.observability.repository.LocatorDriftRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

/** HEAL-3 (FI-HEAL3-B): the drift ranking over observed locator failures. */
class LocatorDriftServiceTest {

    @Test
    void ranksWorstDriftingFirstAndDerivesHealRate() {
        LocatorDriftRepository repo = repo(List.of(
                new Object[]{"#username", 7L, 2L},
                new Object[]{".dashboard", 3L, 3L}));

        List<LocatorDriftEntry> top = service(repo).topDrifting(null);

        assertEquals(2, top.size());
        assertEquals("#username", top.get(0).selector());
        assertEquals(7L, top.get(0).failures());
        assertEquals(2.0 / 7.0, top.get(0).healRate(), 1e-9);
        assertEquals(1.0, top.get(1).healRate(), 1e-9, "3 of 3 proposed → fully healable");
    }

    @Test
    void aLocatorNothingCanFixShowsAZeroHealRateRatherThanBeingHidden() {
        // The point of the ranking: the locator that breaks and cannot be healed is the one worth
        // seeing. Keying the table on successful heals would have made it invisible.
        // Explicit witness: a lone Object[] would otherwise spread as varargs into List<Object>.
        LocatorDriftRepository repo = repo(List.<Object[]>of(new Object[]{"div > span:nth-child(4)", 9L, 0L}));

        LocatorDriftEntry worst = service(repo).topDrifting(null).get(0);

        assertEquals(9L, worst.failures());
        assertEquals(0L, worst.healsProposed());
        assertEquals(0.0, worst.healRate());
    }

    @Test
    void nullHealCountFromTheDatabaseIsTreatedAsZero() {
        LocatorDriftRepository repo = repo(List.<Object[]>of(new Object[]{"#a", 2L, null}));

        assertEquals(0L, service(repo).topDrifting(null).get(0).healsProposed());
    }

    @Test
    void nothingObservedYieldsAnEmptyRankingNotAFabricatedOne() {
        assertTrue(service(repo(List.of())).topDrifting(null).isEmpty());
    }

    @Test
    void clampsTheRequestedLimit() {
        AtomicReference<Pageable> asked = new AtomicReference<>();

        service(capturing(asked)).topDrifting(10_000);

        assertEquals(200, asked.get().getPageSize());
    }

    @Test
    void appliesTheDefaultLimitWhenNoneGiven() {
        AtomicReference<Pageable> asked = new AtomicReference<>();

        service(capturing(asked)).topDrifting(null);

        assertEquals(20, asked.get().getPageSize());
    }

    // --- fakes -----------------------------------------------------------------------------------

    private static LocatorDriftService service(LocatorDriftRepository repo) {
        return new LocatorDriftService(repo, 20, 200);
    }

    private static LocatorDriftRepository repo(List<Object[]> rows) {
        return proxy((method, args) -> "rankByFailureCount".equals(method.getName())
                ? rows : defaultFor(method.getReturnType()));
    }

    private static LocatorDriftRepository capturing(AtomicReference<Pageable> sink) {
        return proxy((method, args) -> {
            if ("rankByFailureCount".equals(method.getName())
                    && args != null && args.length == 1 && args[0] instanceof Pageable p) {
                sink.set(p);
                return List.of();
            }
            return defaultFor(method.getReturnType());
        });
    }

    private interface Handler {
        Object handle(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }

    private static LocatorDriftRepository proxy(Handler handler) {
        return (LocatorDriftRepository) Proxy.newProxyInstance(
                LocatorDriftServiceTest.class.getClassLoader(),
                new Class<?>[]{LocatorDriftRepository.class},
                (p, method, args) -> handler.handle(method, args));
    }

    private static Object defaultFor(Class<?> returnType) {
        if (returnType == boolean.class) return false;
        if (returnType == long.class) return 0L;
        if (returnType == int.class) return 0;
        if (returnType == double.class) return 0.0;
        if (returnType == java.util.Optional.class) return java.util.Optional.empty();
        if (returnType == List.class) return List.of();
        return null;
    }
}
