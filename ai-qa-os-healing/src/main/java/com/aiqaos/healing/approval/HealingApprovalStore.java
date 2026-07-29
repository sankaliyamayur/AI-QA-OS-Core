package com.aiqaos.healing.approval;

import java.util.List;
import java.util.Optional;

/**
 * HEAL-2 seam: stores heals awaiting approval and resolves them. The reference
 * {@link InMemoryHealingApprovalStore} keeps the workflow fully testable; an AI-2-backed store
 * (durable {@code HumanReviewService}/{@code PausedWorkflowRegistry} in {@code orchestration}) is the
 * deferred impl (FI-HEAL2-A) — so {@code healing} needn't depend on {@code orchestration}.
 */
public interface HealingApprovalStore {

    void save(HealingApprovalRequest request);

    Optional<HealingApprovalRequest> find(String healingId);

    /** All requests still in {@code PENDING_APPROVAL}. */
    List<HealingApprovalRequest> pending();
}
