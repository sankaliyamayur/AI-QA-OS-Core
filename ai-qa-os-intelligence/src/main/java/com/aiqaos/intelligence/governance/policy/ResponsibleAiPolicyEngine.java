package com.aiqaos.intelligence.governance.policy;

import com.aiqaos.core.guardrail.GuardrailContext;

/**
 * GOV-3 seam: evaluate a piece of AI content against the Responsible-AI policy and return a
 * {@link PolicyDecision}. The reference {@link RuleBasedResponsibleAiPolicyEngine} is deterministic
 * and config-driven; a live OPA/Rego backend can implement this same seam later (FI-GOV3-A) without
 * touching the guardrail boundary that consumes it.
 */
public interface ResponsibleAiPolicyEngine {

    PolicyDecision evaluate(String content, GuardrailContext context);
}
