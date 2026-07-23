package com.aiqaos.intelligence.component;

import com.aiqaos.core.exception.ValidationException;
import com.aiqaos.core.guardrail.GuardrailContext;
import com.aiqaos.core.guardrail.GuardrailVerdict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The intelligence input boundary. Delegates detection to the SEC-3 {@link PromptInjectionGuardrail}
 * (the shared {@code core} guardrail seam), governed by {@code aiqaos.security.guardrails.*}. Retains
 * a minimal inline fallback so the original behaviour holds if the guardrail bean is absent (e.g.
 * direct construction in a test). {@code PromptCompiler} already calls {@link #scan(String)}.
 */
@Component
public class PromptSecurityGuard {

    private static final Logger log = LoggerFactory.getLogger(PromptSecurityGuard.class);

    @Autowired(required = false)
    private PromptInjectionGuardrail guardrail;

    @Value("${aiqaos.security.guardrails.enabled:true}")
    private boolean enabled;

    @Value("${aiqaos.security.guardrails.mode:enforce}")
    private String mode;

    public void scan(String rawPrompt) {
        if (rawPrompt == null || !enabled) {
            return;
        }
        GuardrailVerdict verdict = evaluate(rawPrompt);
        if (verdict.isAllowed()) {
            return;
        }
        if (isReportOnly()) {
            log.warn("[guardrail:prompt-injection] report-only — {}", verdict.getReason());
            return;
        }
        throw new ValidationException("Potential Prompt Injection / Jailbreak attempt detected: "
                + verdict.getReason());
    }

    /** Wrap untrusted requirement text as inert data (no-op when the guardrail bean is absent). */
    public String sanitizeAndDelimit(String untrusted) {
        return guardrail != null ? guardrail.sanitizeAndDelimit(untrusted) : untrusted;
    }

    private GuardrailVerdict evaluate(String rawPrompt) {
        if (guardrail != null) {
            return guardrail.check(rawPrompt, GuardrailContext.input("prompt-compiler"));
        }
        String lower = rawPrompt.toLowerCase();
        if (lower.contains("ignore previous instructions")
                || lower.contains("system override")
                || lower.contains("jailbreak")) {
            return GuardrailVerdict.block("inline fallback marker");
        }
        return GuardrailVerdict.allow();
    }

    private boolean isReportOnly() {
        return "report".equalsIgnoreCase(mode);
    }
}
