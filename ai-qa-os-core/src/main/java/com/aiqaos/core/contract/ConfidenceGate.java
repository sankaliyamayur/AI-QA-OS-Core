package com.aiqaos.core.contract;

/**
 * AI-1 — the confidence-gate contract.
 *
 * <p>Lives in {@code ai-qa-os-core} so the orchestration pipeline (which invokes it) and the
 * QA Brain (which implements it) can both reference it without a dependency cycle — {@code brain}
 * depends on {@code orchestration}, not the reverse. The Brain owns the decision (Rule 2 / ADR-001);
 * orchestration receives the implementation at runtime via Spring and treats it as optional (a
 * permissive default applies when no implementation is present, e.g. in orchestration's own tests).
 */
public interface ConfidenceGate {

    /**
     * Evaluate a pipeline step's reported confidence and return the routing verdict.
     */
    ConfidenceVerdict evaluate(ConfidenceDecisionContext context);
}
