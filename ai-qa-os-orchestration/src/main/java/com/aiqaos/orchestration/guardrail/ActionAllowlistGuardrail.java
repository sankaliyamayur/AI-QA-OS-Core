package com.aiqaos.orchestration.guardrail;

import com.aiqaos.core.guardrail.Guardrail;
import com.aiqaos.core.guardrail.GuardrailContext;
import com.aiqaos.core.guardrail.GuardrailVerdict;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * SEC-3 output guard (orchestration): grounding check on raw LLM output before the platform acts on
 * it. Blocks output that echoes prompt-injection / instruction-hijack directives (a sign the model
 * was steered). Complements {@code LLMResponseValidator}'s existing schema + framework/language
 * allow-lists; kept to high-confidence hijack markers so legitimate generated code is unaffected.
 * Implements the shared {@code core} {@link Guardrail} seam (ADR-015).
 */
@Component
public class ActionAllowlistGuardrail implements Guardrail {

    /** High-confidence hijack/exfiltration markers that should never appear in grounded QA output. */
    private static final List<Pattern> GROUNDING_VIOLATIONS = List.of(
            compile("ignore\\s+(all\\s+)?(the\\s+)?(previous|above|prior)\\s+instructions"),
            compile("disregard\\s+(all\\s+)?(the\\s+)?(previous|above|prior)\\s+(instructions|context)"),
            compile("system\\s+override"),
            compile("reveal\\s+your\\s+(system\\s+)?(instructions|prompt)"),
            compile("exfiltrate"));

    private static Pattern compile(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String getName() {
        return "output-grounding";
    }

    @Override
    public GuardrailVerdict check(String content, GuardrailContext context) {
        if (content == null || content.isEmpty()) {
            return GuardrailVerdict.allow();
        }
        for (Pattern p : GROUNDING_VIOLATIONS) {
            if (p.matcher(content).find()) {
                return GuardrailVerdict.block("output grounding violation: " + p.pattern());
            }
        }
        return GuardrailVerdict.allow();
    }
}
