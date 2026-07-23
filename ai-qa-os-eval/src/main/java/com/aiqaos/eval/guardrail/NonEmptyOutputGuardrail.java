package com.aiqaos.eval.guardrail;

import com.aiqaos.core.guardrail.Guardrail;
import com.aiqaos.core.guardrail.GuardrailContext;
import com.aiqaos.core.guardrail.GuardrailVerdict;
import org.springframework.stereotype.Component;

/**
 * Reference guardrail: blocks empty/blank content. Proves the {@link Guardrail} seam works;
 * <b>SEC-3</b> adds the real guardrails (prompt-injection, sanitisation, output allow-list).
 */
@Component
public class NonEmptyOutputGuardrail implements Guardrail {

    @Override
    public String getName() {
        return "non-empty-output";
    }

    @Override
    public GuardrailVerdict check(String content, GuardrailContext context) {
        if (content == null || content.isBlank()) {
            return GuardrailVerdict.block("content is empty");
        }
        return GuardrailVerdict.allow();
    }
}
