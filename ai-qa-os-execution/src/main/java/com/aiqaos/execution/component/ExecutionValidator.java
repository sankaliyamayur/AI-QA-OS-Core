package com.aiqaos.execution.component;

import com.aiqaos.core.contract.ExecutionRequest;
import com.aiqaos.core.exception.ValidationException;
import com.aiqaos.core.guardrail.GuardrailContext;
import com.aiqaos.core.guardrail.GuardrailVerdict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExecutionValidator {

    private static final Logger log = LoggerFactory.getLogger(ExecutionValidator.class);

    // SEC-3: script-surface guard (null-safe — skipped if the bean is absent, e.g. direct construction).
    @Autowired(required = false)
    private ScriptSurfaceGuardrail scriptGuardrail;

    @Value("${aiqaos.security.guardrails.enabled:true}")
    private boolean guardrailsEnabled;

    @Value("${aiqaos.security.guardrails.mode:enforce}")
    private String guardrailsMode;

    public void validate(ExecutionRequest request) {
        if (request.getScriptContent() == null || request.getScriptContent().trim().isEmpty()) {
            throw new ValidationException("Script content cannot be null or empty.");
        }

        // SEC-3: refuse scripts with out-of-surface shell/network/eval calls before execution.
        if (scriptGuardrail != null && guardrailsEnabled) {
            GuardrailVerdict verdict = scriptGuardrail.check(
                    request.getScriptContent(), GuardrailContext.output("execution"));
            if (!verdict.isAllowed()) {
                if ("report".equalsIgnoreCase(guardrailsMode)) {
                    log.warn("[guardrail:script-surface] report-only — {}", verdict.getReason());
                } else {
                    throw new ValidationException("Script surface violation: " + verdict.getReason());
                }
            }
        }
    }
}
