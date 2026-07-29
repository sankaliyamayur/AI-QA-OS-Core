package com.aiqaos.intelligence.version;

import com.aiqaos.core.guardrail.GuardrailContext;
import com.aiqaos.intelligence.governance.policy.PolicyDecision;
import com.aiqaos.intelligence.governance.policy.ResponsibleAiPolicyEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * GOV-3 × GOV-4: closes the governance loop by policy-gating a version promotion. Before the GOV-4
 * {@link VersionRegistry} pins a version as active, its content is checked against the GOV-3
 * Responsible-AI policy — a violating version (PII, production URLs, destructive content, …) is
 * <b>blocked from promotion</b>, so a governed pin can never activate content the policy forbids.
 *
 * <p>Both halves live in {@code ai-qa-os-intelligence}, so this is an in-module connection — no new
 * dependency edge.
 */
@Service
public class GovernedVersionPromoter {

    private static final Logger log = LoggerFactory.getLogger(GovernedVersionPromoter.class);

    private final VersionRegistry registry;
    private final ResponsibleAiPolicyEngine policyEngine;

    public GovernedVersionPromoter(VersionRegistry registry, ResponsibleAiPolicyEngine policyEngine) {
        this.registry = registry;
        this.policyEngine = policyEngine;
    }

    /**
     * Promote {@code versionTag} for {@code registryKey} only if its {@code content} clears the
     * Responsible-AI policy; otherwise block the promotion (no pin) and return why.
     */
    public VersionPromotionDecision promote(String registryKey, VersionKind kind, String versionTag,
                                            String actor, String content) {
        PolicyDecision policy = policyEngine.evaluate(content,
                GuardrailContext.input("version-promotion:" + registryKey));

        if (!policy.isPermitted()) {
            String reason = "blocked by Responsible-AI policy [" + policy.getRuleId() + "]: "
                    + policy.getReason();
            log.warn("[GovernedPromotion] {} v{} {} — not pinned", registryKey, versionTag, reason);
            return VersionPromotionDecision.blocked(reason, policy);
        }

        VersionPin pin = registry.pin(registryKey, kind, versionTag, actor);
        return VersionPromotionDecision.promoted(pin);
    }
}
