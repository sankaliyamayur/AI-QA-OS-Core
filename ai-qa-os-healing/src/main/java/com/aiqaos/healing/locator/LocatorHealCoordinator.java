package com.aiqaos.healing.locator;

import com.aiqaos.healing.approval.HealingApprovalDecision;
import com.aiqaos.healing.approval.HealingApprovalService;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * HEAL-1 × HEAL-2: bridges the two halves of the self-healing loop. HEAL-1's {@link LocatorHealer}
 * proposes the best replacement locator; HEAL-2's {@link HealingApprovalService} then governs it —
 * auto-approving an unambiguous heal, or routing anything softer to human approval (never silently
 * editing a script). One call, one governed decision.
 */
@Service
public class LocatorHealCoordinator {

    private final LocatorHealer healer;
    private final HealingApprovalService approvalService;
    private final AtomicLong sequence = new AtomicLong();

    public LocatorHealCoordinator(LocatorHealer healer, HealingApprovalService approvalService) {
        this.healer = healer;
        this.approvalService = approvalService;
    }

    public LocatorHealResult heal(LocatorHealingRequest request) {
        List<LocatorCandidate> candidates = healer.propose(request);
        if (candidates.isEmpty()) {
            return LocatorHealResult.noCandidate();
        }
        LocatorCandidate best = candidates.get(0);
        String healingId = "LOC-HEAL-" + sequence.incrementAndGet();
        String summary = "replace '" + (request != null ? request.getBrokenLocator() : "?")
                + "' with " + best.getStrategy() + " '" + best.getValue() + "'";
        String correlationId = request != null ? request.getCorrelationId() : null;

        HealingApprovalDecision decision =
                approvalService.decide(healingId, summary, best.getConfidence(), correlationId);
        return new LocatorHealResult(best, decision);
    }
}
