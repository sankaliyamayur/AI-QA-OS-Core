package com.aiqaos.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.learning.dashboard.LearningDashboardAssembler;
import com.aiqaos.learning.dashboard.LearningDashboardView;
import com.aiqaos.learning.dashboard.LearningHealth;
import com.aiqaos.learning.metrics.LearningMetricsCalculator;
import com.aiqaos.learning.metrics.LearningMetricsProperties;
import com.aiqaos.learning.metrics.LearningObservationEntity;
import com.aiqaos.learning.metrics.LearningObservationRepository;
import com.aiqaos.learning.metrics.LearningTrend;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

/**
 * LRN-3 (FI-LRN3-A): the read path over the observations the run pipeline now records. The
 * calculator and assembler are the real ones (both pure) — only the repository is faked, so these
 * tests exercise the composition exactly as it runs.
 */
class LearningDashboardServiceTest {

    @Test
    void reversesTheNewestFirstWindowSoTheTrendIsNotInverted() {
        // Chronologically: two failures, then two successes — a loop that is improving.
        // The DB hands them back newest-first, which is the opposite order.
        LearningObservationRepository repo = repo(List.of(
                observation(4, true, 0.9),
                observation(3, true, 0.9),
                observation(2, false, 0.4),
                observation(1, false, 0.4)));

        LearningDashboardView view = service(repo).getView(null);

        assertEquals(LearningTrend.IMPROVING, view.getTrend(),
                "computed newest-first this reads as REGRESSING — order into the calculator is load-bearing");
        assertEquals(4, view.getSampleCount());
    }

    @Test
    void plotsTheConfidenceSeriesInRunOrder() {
        LearningObservationRepository repo = repo(List.of(
                observation(3, true, 0.30),
                observation(2, true, 0.20),
                observation(1, true, 0.10)));

        assertEquals(List.of(0.10, 0.20, 0.30), service(repo).getView(null).getConfidenceHistory(),
                "the chart must read oldest → newest");
    }

    @Test
    void noObservationsReportsNoDataRatherThanAFabricatedZeroScore() {
        LearningDashboardView view = service(repo(List.of())).getView(null);

        assertEquals(0, view.getSampleCount());
        assertEquals("No learning data yet", view.getHeadline(),
                "an unmeasured loop must not be reported as a 0.00 score");
    }

    @Test
    void surfacesAtRiskWhenTheLoopIsRegressing() {
        LearningObservationRepository repo = repo(List.of(
                observation(4, false, 0.3),
                observation(3, false, 0.3),
                observation(2, true, 0.9),
                observation(1, true, 0.9)));

        LearningDashboardView view = service(repo).getView(null);

        assertEquals(LearningTrend.REGRESSING, view.getTrend());
        assertEquals(LearningHealth.AT_RISK, view.getHealth());
        assertTrue(view.getHeadline().startsWith("Learning at risk"));
    }

    @Test
    void clampsTheRequestedWindowToTheConfiguredMaximum() {
        AtomicReference<Pageable> asked = new AtomicReference<>();

        service(capturing(asked)).getView(10_000);

        assertEquals(500, asked.get().getPageSize(), "a caller cannot ask for the whole table");
    }

    @Test
    void appliesTheDefaultWindowWhenNoneGiven() {
        AtomicReference<Pageable> asked = new AtomicReference<>();

        service(capturing(asked)).getView(null);

        assertEquals(200, asked.get().getPageSize());
    }

    // --- fakes -----------------------------------------------------------------------------------

    private static LearningDashboardService service(LearningObservationRepository repo) {
        return new LearningDashboardService(
                repo,
                new LearningMetricsCalculator(new LearningMetricsProperties()),
                new LearningDashboardAssembler(),
                200, 500);
    }

    private static LearningObservationEntity observation(long sequenceNo, boolean success, double confidence) {
        LearningObservationEntity e = new LearningObservationEntity();
        e.setSequenceNo(sequenceNo);
        e.setSuccess(success);
        e.setConfidence(confidence);
        e.setLabel("corr-" + sequenceNo);
        return e;
    }

    /** @param newestFirst as the repository returns them — highest sequence_no first */
    private static LearningObservationRepository repo(List<LearningObservationEntity> newestFirst) {
        return proxy((method, args) -> {
            if ("findAllByOrderBySequenceNoDesc".equals(method.getName())) {
                return new ArrayList<>(newestFirst);
            }
            return defaultFor(method.getReturnType());
        });
    }

    private static LearningObservationRepository capturing(AtomicReference<Pageable> sink) {
        return proxy((method, args) -> {
            if ("findAllByOrderBySequenceNoDesc".equals(method.getName())
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

    private static LearningObservationRepository proxy(Handler handler) {
        return (LearningObservationRepository) Proxy.newProxyInstance(
                LearningDashboardServiceTest.class.getClassLoader(),
                new Class<?>[]{LearningObservationRepository.class},
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
