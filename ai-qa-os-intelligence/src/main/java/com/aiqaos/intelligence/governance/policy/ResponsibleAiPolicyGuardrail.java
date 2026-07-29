package com.aiqaos.intelligence.governance.policy;

import com.aiqaos.core.guardrail.Guardrail;
import com.aiqaos.core.guardrail.GuardrailContext;
import com.aiqaos.core.guardrail.GuardrailVerdict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * GOV-3: enforces the Responsible-AI policy at the SEC-3 {@link Guardrail} boundary. Discovered as a
 * {@code Guardrail} bean alongside the SEC-3 guards ({@code PromptInjectionGuardrail}, …), so it runs
 * wherever guardrails already run — no new wiring.
 *
 * <p>Mapping is <b>fail-safe</b>: {@code BLOCK} and {@code REQUIRE_REVIEW} both become a
 * {@link GuardrailVerdict#block}, so a review-required action is never silently allowed (routing
 * {@code REQUIRE_REVIEW} into the AI-1/AI-2 human-review path instead of blocking is FI-GOV3-B).
 * {@code warn} mode logs and allows; {@code enabled=false} allows everything.
 */
@Component
public class ResponsibleAiPolicyGuardrail implements Guardrail {

    private static final Logger log = LoggerFactory.getLogger(ResponsibleAiPolicyGuardrail.class);

    private final ResponsibleAiPolicyEngine engine;
    private final ResponsibleAiPolicyProperties properties;

    public ResponsibleAiPolicyGuardrail(ResponsibleAiPolicyEngine engine,
                                        ResponsibleAiPolicyProperties properties) {
        this.engine = engine;
        this.properties = properties;
    }

    @Override
    public String getName() {
        return "responsible-ai-policy";
    }

    @Override
    public GuardrailVerdict check(String content, GuardrailContext context) {
        if (!properties.isEnabled()) {
            return GuardrailVerdict.allow();
        }
        PolicyDecision decision = engine.evaluate(content, context);
        if (decision.isPermitted()) {
            return GuardrailVerdict.allow();
        }
        // A violating decision (BLOCK or REQUIRE_REVIEW).
        if (!properties.isEnforce()) {
            log.warn("Responsible-AI policy [{}] would block (warn mode): {}",
                    decision.getRuleId(), decision.getReason());
            return GuardrailVerdict.allow();
        }
        String reason = "Responsible-AI policy [" + decision.getRuleId() + "]: " + decision.getReason();
        return GuardrailVerdict.block(reason);
    }
}
