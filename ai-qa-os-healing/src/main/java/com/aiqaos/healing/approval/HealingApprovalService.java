package com.aiqaos.healing.approval;

import com.aiqaos.core.contract.ConfidenceDecisionContext;
import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.ConfidenceVerdict;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * HEAL-2: tiers a heal by confidence and runs the approval lifecycle for the highest-risk autonomous
 * action (auto-editing a script). <b>Strict (§0.4-A):</b> only a full {@code PROCEED} auto-applies;
 * {@code PROCEED_WITH_VALIDATION}/{@code HUMAN_REVIEW} require human approval; {@code UNGATED}
 * (unreported confidence) is rejected. Reuses the AI-1 {@link ConfidenceGate}; when it is absent, a
 * local {@code autoApprove}/{@code reviewFloor} threshold decides. Pending heals are recorded in the
 * {@link HealingApprovalStore} and resolved via {@link #approve}/{@link #reject}.
 */
@Service
public class HealingApprovalService {

    private static final Logger log = LoggerFactory.getLogger(HealingApprovalService.class);

    private final HealingApprovalStore store;
    private final HealingApprovalProperties properties;
    private final AtomicLong sequence = new AtomicLong();

    @Autowired(required = false)
    private ConfidenceGate confidenceGate;

    @Autowired
    public HealingApprovalService(HealingApprovalStore store, HealingApprovalProperties properties) {
        this.store = store;
        this.properties = properties;
    }

    /** Explicit-gate constructor (also used by tests / non-Spring wiring). */
    public HealingApprovalService(HealingApprovalStore store, HealingApprovalProperties properties,
                                  ConfidenceGate gate) {
        this(store, properties);
        this.confidenceGate = gate;
    }

    public HealingApprovalDecision decide(String healingId, String summary, double confidence,
                                          String correlationId) {
        HealingApprovalStatus status;
        ConfidenceVerdict verdict = null;
        String reason;

        if (confidenceGate != null) {
            verdict = confidenceGate.evaluate(
                    new ConfidenceDecisionContext("heal-approval", confidence, correlationId));
            switch (verdict) {
                case PROCEED -> { status = HealingApprovalStatus.AUTO_APPROVED; }
                case PROCEED_WITH_VALIDATION, HUMAN_REVIEW -> { status = HealingApprovalStatus.PENDING_APPROVAL; }
                default -> { status = HealingApprovalStatus.REJECTED; } // UNGATED → fail-safe
            }
            reason = "confidence gate → " + verdict + " (strict: only PROCEED auto-applies)";
        } else {
            if (confidence >= properties.getAutoApprove()) {
                status = HealingApprovalStatus.AUTO_APPROVED;
            } else if (confidence >= properties.getReviewFloor()) {
                status = HealingApprovalStatus.PENDING_APPROVAL;
            } else {
                status = HealingApprovalStatus.REJECTED;
            }
            reason = String.format("local threshold (auto %.2f / floor %.2f), confidence %.2f",
                    properties.getAutoApprove(), properties.getReviewFloor(), confidence);
        }

        if (status == HealingApprovalStatus.PENDING_APPROVAL) {
            store.save(new HealingApprovalRequest(healingId, summary, confidence, correlationId,
                    sequence.incrementAndGet(), status));
            log.warn("[HealingApproval] {} pending human approval — {} — {}", healingId, summary, reason);
        }
        return new HealingApprovalDecision(healingId, status, reason, verdict);
    }

    /** Pending heals awaiting a human decision. */
    public List<HealingApprovalRequest> pending() {
        return store.pending();
    }

    /** Approve a pending heal (→ {@code APPROVED}). Returns false if not pending / not found. */
    public boolean approve(String healingId) {
        return resolve(healingId, HealingApprovalStatus.APPROVED);
    }

    /** Reject a pending heal (→ {@code REJECTED}). Returns false if not pending / not found. */
    public boolean reject(String healingId) {
        return resolve(healingId, HealingApprovalStatus.REJECTED);
    }

    private boolean resolve(String healingId, HealingApprovalStatus terminal) {
        Optional<HealingApprovalRequest> found = store.find(healingId);
        if (found.isEmpty() || found.get().getStatus() != HealingApprovalStatus.PENDING_APPROVAL) {
            return false;
        }
        HealingApprovalRequest request = found.get();
        request.setStatus(terminal);
        store.save(request);
        return true;
    }
}
