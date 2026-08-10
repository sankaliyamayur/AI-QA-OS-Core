package com.aiqaos.orchestration.healing;

import com.aiqaos.core.failure.BrokenLocatorSignal;
import com.aiqaos.healing.locator.LocatorHealCoordinator;
import com.aiqaos.healing.locator.LocatorHealResult;
import com.aiqaos.healing.locator.LocatorHealingRequest;
import com.aiqaos.observability.entity.LocatorDriftEntity;
import com.aiqaos.observability.repository.LocatorDriftRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * HEAL-3 (FI-HEAL3-A): the missing link in the locator-healing loop.
 *
 * <p>ADR-072 found this chain broken at every joint: nothing produced a structured broken locator,
 * {@link LocatorHealCoordinator} had zero production callers, {@code HealingMemory} was never
 * written to, and no enumerable store carried locator identity. The producer now exists (ADR-094 —
 * Playwright's call log names the selector it failed on), so this closes the rest: **observe →
 * attempt a governed heal → persist**, which is what makes the drift ranking (FI-HEAL3-B) possible.
 *
 * <p><b>The observation is recorded whether or not healing succeeds.</b> Drift is how often a
 * locator broke; the heal outcome is extra. Recording only successful heals would systematically
 * hide the locators that break and cannot be fixed — the ones most worth looking at.
 *
 * <p><b>Opt-in and best-effort.</b> Registered only when
 * {@code aiqaos.healing.locator-drift.enabled=true}, and it never throws: a drift datapoint must
 * never fail the run it describes. Same discipline as LRN-3's observation recorder.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.healing.locator-drift.enabled", havingValue = "true")
public class LocatorDriftRecorder {

    private static final Logger log = LoggerFactory.getLogger(LocatorDriftRecorder.class);

    private final LocatorDriftRepository repository;
    private final LocatorHealCoordinator healCoordinator;

    public LocatorDriftRecorder(LocatorDriftRepository repository,
                                LocatorHealCoordinator healCoordinator) {
        this.repository = repository;
        this.healCoordinator = healCoordinator;
    }

    /**
     * Record every locator this run failed on, attempting a governed heal for each.
     *
     * @return how many observations were persisted
     */
    public int record(List<BrokenLocatorSignal> signals, UUID executionId, String correlationId) {
        if (signals == null || signals.isEmpty()) {
            return 0;
        }
        int recorded = 0;
        for (BrokenLocatorSignal signal : signals) {
            try {
                repository.save(toEntity(signal, executionId, correlationId, attemptHeal(signal, correlationId)));
                recorded++;
            } catch (Exception e) {
                // Never propagate: a drift datapoint must not fail the run it describes.
                log.warn("[locator-drift] failed to record '{}': {}", signal.getSelector(), e.getMessage());
            }
        }
        if (recorded > 0) {
            log.info("[locator-drift] recorded {} observed locator failure(s) for execution {}",
                    recorded, executionId);
        }
        return recorded;
    }

    /**
     * Ask HEAL-1/HEAL-2 for a governed replacement. A failure here is not fatal — the observation is
     * still worth persisting, and returning null simply means "no proposal", which is exactly what a
     * genuinely unfixable locator looks like.
     */
    private LocatorHealResult attemptHeal(BrokenLocatorSignal signal, String correlationId) {
        try {
            LocatorHealingRequest request = new LocatorHealingRequest(
                    signal.getSelector(), null, signal.getTestCaseId(), Map.of(), correlationId);
            return healCoordinator.heal(request);
        } catch (Exception e) {
            log.warn("[locator-drift] heal attempt failed for '{}': {}", signal.getSelector(), e.getMessage());
            return null;
        }
    }

    private LocatorDriftEntity toEntity(BrokenLocatorSignal signal, UUID executionId,
                                        String correlationId, LocatorHealResult heal) {
        LocatorDriftEntity entity = new LocatorDriftEntity();
        entity.setSelector(signal.getSelector());
        entity.setTestCaseId(signal.getTestCaseId());
        entity.setFailingAction(signal.getFailingAction());
        entity.setProvenance(signal.getProvenance().name());
        entity.setExecutionId(executionId);
        entity.setCorrelationId(correlationId);
        entity.setObservedAt(LocalDateTime.now());
        // tenant_id is stamped by Hibernate's @TenantId discriminator (ADR-054/057).

        // A proposal identical to the broken locator is not a heal. HEAL-1 works from the element's
        // attributes, and the call log supplies none, so its only move is to "relax" the broken
        // locator — which for a plain #id returns the same string. Recording that would inflate the
        // heal rate and make an unfixable locator look serviceable, which is the exact opposite of
        // what the ranking is for.
        if (heal != null && heal.getChosen() != null
                && !signal.getSelector().equals(heal.getChosen().getValue())) {
            entity.setHealedTo(heal.getChosen().getValue());
            entity.setHealStrategy(String.valueOf(heal.getChosen().getStrategy()));
            if (heal.getDecision() != null) {
                entity.setHealApproval(String.valueOf(heal.getDecision().getStatus()));
            }
        }
        return entity;
    }
}
