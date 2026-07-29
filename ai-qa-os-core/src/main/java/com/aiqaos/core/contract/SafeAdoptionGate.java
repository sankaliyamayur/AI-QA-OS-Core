package com.aiqaos.core.contract;

/**
 * LRN-4 — the safe-adoption gate contract.
 *
 * <p>Lives in {@code ai-qa-os-core} so learning (which produces candidates) and the QA Brain (which
 * decides — Rule 2 / ADR-001) can both reference it without a dependency cycle: {@code brain} depends
 * on {@code core}, not on {@code ai-qa-os-learning} or {@code ai-qa-os-eval}. This mirrors the AI-1
 * {@link ConfidenceGate} placement (ADR-010).
 *
 * <p>A learned improvement is <b>admitted</b> only if it passes the evaluation threshold and clears
 * the confidence gate; every other outcome is {@code REJECTED_FOR_REVIEW} — so continuous learning
 * can raise quality but never silently lower it.
 */
public interface SafeAdoptionGate {

    AdoptionDecision evaluate(AdoptionCandidate candidate);
}
