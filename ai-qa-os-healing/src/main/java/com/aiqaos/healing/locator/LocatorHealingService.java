package com.aiqaos.healing.locator;

import com.aiqaos.core.contract.ConfidenceDecisionContext;
import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.ConfidenceVerdict;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * HEAL-1: the governed locator-healing entry point. Proposes ranked candidates via the
 * {@link LocatorHealer}, then decides whether the best may be <b>auto-applied</b> by clearing the
 * reused AI-1 {@link ConfidenceGate} — {@code PROCEED}/{@code PROCEED_WITH_VALIDATION} apply, anything
 * else is surfaced for review (HEAL-2), never silently applied. When no gate is wired, a local
 * {@code minConfidence} threshold decides instead.
 */
@Service
public class LocatorHealingService {

    private static final Logger log = LoggerFactory.getLogger(LocatorHealingService.class);

    private final LocatorHealer healer;
    private final LocatorHealingProperties properties;

    /** Optional so healing works whether or not the Brain's confidence gate is on the classpath. */
    @Autowired(required = false)
    private ConfidenceGate confidenceGate;

    @Autowired
    public LocatorHealingService(LocatorHealer healer, LocatorHealingProperties properties) {
        this.healer = healer;
        this.properties = properties;
    }

    /** Test seam: supply the confidence gate explicitly. */
    LocatorHealingService(LocatorHealer healer, LocatorHealingProperties properties, ConfidenceGate gate) {
        this(healer, properties);
        this.confidenceGate = gate;
    }

    public LocatorHealingOutcome heal(LocatorHealingRequest request) {
        List<LocatorCandidate> candidates = healer.propose(request);
        if (candidates.isEmpty()) {
            return LocatorHealingOutcome.none("no candidate locator could be proposed");
        }
        LocatorCandidate best = candidates.get(0);

        boolean applied;
        ConfidenceVerdict verdict = null;
        String reason;

        if (confidenceGate != null) {
            verdict = confidenceGate.evaluate(new ConfidenceDecisionContext(
                    "locator-heal", best.getConfidence(),
                    request != null ? request.getCorrelationId() : null));
            applied = verdict == ConfidenceVerdict.PROCEED
                    || verdict == ConfidenceVerdict.PROCEED_WITH_VALIDATION;
            reason = "confidence gate → " + verdict;
        } else {
            applied = best.getConfidence() >= properties.getMinConfidence();
            reason = applied
                    ? String.format("confidence %.2f ≥ local min %.2f", best.getConfidence(), properties.getMinConfidence())
                    : String.format("confidence %.2f < local min %.2f", best.getConfidence(), properties.getMinConfidence());
        }

        if (!applied) {
            log.warn("[LocatorHealer] not auto-applied ({} '{}') — {} — surfaced for review",
                    best.getStrategy(), best.getValue(), reason);
        }
        return new LocatorHealingOutcome(candidates, best, applied, verdict, reason);
    }
}
