package com.aiqaos.orchestration.healing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.core.failure.BrokenLocatorSignal;
import com.aiqaos.healing.approval.HealingApprovalDecision;
import com.aiqaos.healing.approval.HealingApprovalStatus;
import com.aiqaos.healing.locator.LocatorCandidate;
import com.aiqaos.healing.locator.LocatorHealCoordinator;
import com.aiqaos.healing.locator.LocatorHealResult;
import com.aiqaos.healing.locator.LocatorHealingRequest;
import com.aiqaos.healing.locator.LocatorStrategy;
import com.aiqaos.observability.entity.LocatorDriftEntity;
import com.aiqaos.observability.repository.LocatorDriftRepository;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** HEAL-3 (FI-HEAL3-A): observe → governed heal → persist. */
class LocatorDriftRecorderTest {

    private static final BrokenLocatorSignal SIGNAL = new BrokenLocatorSignal(
            "TC-001", "#username", "page.fill", BrokenLocatorSignal.Provenance.PLAYWRIGHT_CALL_LOG);

    @Test
    void persistsTheObservationWithItsProvenance() {
        Captured captured = new Captured();
        LocatorDriftRecorder recorder = new LocatorDriftRecorder(
                repo(captured), coordinator(candidate("[data-testid=\"user\"]")));

        int recorded = recorder.record(List.of(SIGNAL), UUID.randomUUID(), "corr-1");

        assertEquals(1, recorded);
        LocatorDriftEntity saved = captured.saved.get(0);
        assertEquals("#username", saved.getSelector());
        assertEquals("page.fill", saved.getFailingAction());
        assertEquals("PLAYWRIGHT_CALL_LOG", saved.getProvenance());
        assertEquals("corr-1", saved.getCorrelationId());
        assertEquals("[data-testid=\"user\"]", saved.getHealedTo());
        assertEquals("AUTO_APPROVED", saved.getHealApproval());
    }

    @Test
    void aProposalIdenticalToTheBrokenLocatorIsNotAHeal() {
        // Found live: with no element attributes to work from, HEAL-1 can only "relax" the broken
        // locator, which for a plain #id returns the same string. Counting it would inflate the heal
        // rate and make an unfixable locator look serviceable.
        Captured captured = new Captured();
        LocatorDriftRecorder recorder = new LocatorDriftRecorder(
                repo(captured), coordinator(candidate("#username")));

        recorder.record(List.of(SIGNAL), UUID.randomUUID(), "corr-1");

        LocatorDriftEntity saved = captured.saved.get(0);
        assertEquals("#username", saved.getSelector(), "the observation is still recorded");
        assertNull(saved.getHealedTo(), "but not as a heal");
        assertNull(saved.getHealApproval());
    }

    @Test
    void theObservationIsRecordedEvenWhenNothingCanHealIt() {
        Captured captured = new Captured();
        LocatorDriftRecorder recorder = new LocatorDriftRecorder(
                repo(captured), coordinator(LocatorHealResult.noCandidate()));

        assertEquals(1, recorder.record(List.of(SIGNAL), UUID.randomUUID(), "corr-1"));
        assertNull(captured.saved.get(0).getHealedTo());
    }

    @Test
    void aHealerThatThrowsDoesNotLoseTheObservation() {
        Captured captured = new Captured();
        LocatorDriftRecorder recorder = new LocatorDriftRecorder(repo(captured), throwingCoordinator());

        assertEquals(1, recorder.record(List.of(SIGNAL), UUID.randomUUID(), "corr-1"),
                "the drift observation is the valuable part; a failed heal must not discard it");
        assertNull(captured.saved.get(0).getHealedTo());
    }

    @Test
    void aRepositoryFailureNeverBreaksTheRunItDescribes() {
        LocatorDriftRepository failing = (LocatorDriftRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{LocatorDriftRepository.class},
                (p, m, a) -> { throw new IllegalStateException("db down"); });

        assertEquals(0, new LocatorDriftRecorder(failing, coordinator(candidate("#x")))
                .record(List.of(SIGNAL), UUID.randomUUID(), "c"));
    }

    @Test
    void noSignalsMeansNoWrites() {
        Captured captured = new Captured();
        LocatorDriftRecorder recorder = new LocatorDriftRecorder(repo(captured), coordinator(candidate("#x")));

        assertEquals(0, recorder.record(List.of(), UUID.randomUUID(), "c"));
        assertEquals(0, recorder.record(null, UUID.randomUUID(), "c"));
        assertTrue(captured.saved.isEmpty());
    }

    // --- fakes -----------------------------------------------------------------------------------

    private static final class Captured {
        final List<LocatorDriftEntity> saved = new ArrayList<>();
    }

    private static LocatorHealResult candidate(String value) {
        return new LocatorHealResult(
                new LocatorCandidate(value, LocatorStrategy.TEST_ID, 0.9, "test"),
                new HealingApprovalDecision("H-1", HealingApprovalStatus.AUTO_APPROVED, "ok", null));
    }

    private static LocatorHealCoordinator coordinator(LocatorHealResult result) {
        // Nulls are safe: the constructor only assigns, and heal(...) is overridden below.
        return new LocatorHealCoordinator(null, null) {
            @Override
            public LocatorHealResult heal(LocatorHealingRequest request) {
                return result;
            }
        };
    }

    private static LocatorHealCoordinator throwingCoordinator() {
        // Nulls are safe: the constructor only assigns, and heal(...) is overridden below.
        return new LocatorHealCoordinator(null, null) {
            @Override
            public LocatorHealResult heal(LocatorHealingRequest request) {
                throw new IllegalStateException("healer exploded");
            }
        };
    }

    private static LocatorDriftRepository repo(Captured captured) {
        return (LocatorDriftRepository) Proxy.newProxyInstance(
                LocatorDriftRecorderTest.class.getClassLoader(),
                new Class<?>[]{LocatorDriftRepository.class},
                (p, method, args) -> {
                    if ("save".equals(method.getName())) {
                        captured.saved.add((LocatorDriftEntity) args[0]);
                        return args[0];
                    }
                    return null;
                });
    }
}
