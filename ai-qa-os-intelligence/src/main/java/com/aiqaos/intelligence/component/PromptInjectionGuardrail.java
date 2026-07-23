package com.aiqaos.intelligence.component;

import com.aiqaos.core.guardrail.Guardrail;
import com.aiqaos.core.guardrail.GuardrailContext;
import com.aiqaos.core.guardrail.GuardrailVerdict;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * SEC-3 input guard (intelligence): detects prompt-injection / jailbreak attempts in prompt text
 * and delimits untrusted requirement text so the model treats it as <em>data, not instructions</em>.
 * Implements the shared {@code core} {@link Guardrail} seam (ADR-015); wired in via
 * {@link PromptSecurityGuard}, which {@code PromptCompiler} already calls.
 */
@Component
public class PromptInjectionGuardrail implements Guardrail {

    static final String DELIM_OPEN = "[[UNTRUSTED_REQUIREMENT]]";
    static final String DELIM_CLOSE = "[[/UNTRUSTED_REQUIREMENT]]";

    /** High-confidence injection/jailbreak markers (kept conservative to avoid false positives). */
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            compile("ignore\\s+(all\\s+)?(the\\s+)?(previous|above|prior)\\s+instructions"),
            compile("disregard\\s+(all\\s+)?(the\\s+)?(previous|above|prior)\\s+(instructions|context)"),
            compile("ignore\\s+your\\s+(previous\\s+)?instructions"),
            compile("system\\s+override"),
            compile("jailbreak"),
            compile("developer\\s+mode"),
            compile("reveal\\s+your\\s+(system\\s+)?(instructions|prompt)"),
            compile("(print|show|repeat)\\s+your\\s+(system\\s+)?(instructions|prompt)"),
            compile("what\\s+(is|are)\\s+your\\s+(system\\s+)?(instructions|prompt)"),
            compile("exfiltrate"),
            compile("you\\s+are\\s+now\\s+(a\\s+)?(different|new)"));

    private static Pattern compile(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String getName() {
        return "prompt-injection";
    }

    @Override
    public GuardrailVerdict check(String content, GuardrailContext context) {
        if (content == null || content.isEmpty()) {
            return GuardrailVerdict.allow();
        }
        for (Pattern p : INJECTION_PATTERNS) {
            if (p.matcher(content).find()) {
                return GuardrailVerdict.block("prompt-injection marker: " + p.pattern());
            }
        }
        return GuardrailVerdict.allow();
    }

    /**
     * Wrap untrusted requirement text in explicit delimiters and neutralise any attempt to forge or
     * close them, so downstream prompt composition presents it as inert data.
     */
    public String sanitizeAndDelimit(String untrusted) {
        String cleaned = untrusted == null ? "" : untrusted
                .replace(DELIM_OPEN, "")
                .replace(DELIM_CLOSE, "");
        return DELIM_OPEN + "\n" + cleaned + "\n" + DELIM_CLOSE;
    }
}
