package com.aiqaos.core.contract;

/**
 * AI-1 — outcome of a confidence-gate evaluation.
 *
 * <ul>
 *   <li>{@code PROCEED} — confidence &ge; high threshold; auto-proceed.</li>
 *   <li>{@code PROCEED_WITH_VALIDATION} — medium &le; confidence &lt; high; proceed but flag for validation.</li>
 *   <li>{@code HUMAN_REVIEW} — confidence &lt; medium; route to human review (AI-2).</li>
 *   <li>{@code UNGATED} — confidence not reported (&le; 0); the gate does not apply.</li>
 * </ul>
 */
public enum ConfidenceVerdict {
    PROCEED,
    PROCEED_WITH_VALIDATION,
    HUMAN_REVIEW,
    UNGATED
}
