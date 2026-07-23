package com.aiqaos.core.guardrail;

/**
 * A safety check over content flowing into or out of the AI. This is a cross-cutting contract in
 * {@code core} (like {@code ConfidenceGate}, ADR-010): MOD-3's {@code ai-qa-os-eval} defines the
 * reference guardrails home, and SEC-3 fills it with real guards in {@code intelligence}
 * (input sanitisation), {@code orchestration} (output grounding), and {@code execution}
 * (script surface) — all implementing this one seam. Promoted here from {@code eval} so every
 * module can implement it without a dependency cycle (ADR-015).
 */
public interface Guardrail {

    /** Stable, human-readable guardrail name. */
    String getName();

    /** Inspect {@code content} in the given {@link GuardrailContext} and return a verdict. */
    GuardrailVerdict check(String content, GuardrailContext context);
}
