package com.aiqaos.execution.component;

import com.aiqaos.core.guardrail.Guardrail;
import com.aiqaos.core.guardrail.GuardrailContext;
import com.aiqaos.core.guardrail.GuardrailVerdict;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * SEC-3 execution guard: refuses a generated script that contains shell / process / filesystem-escape
 * / dynamic-eval calls outside the legitimate test-automation surface. A deny-list (not an allow-list)
 * because test scripts use a broad framework API; the risk is process/shell escape, never legitimate
 * in a Playwright/Selenium/RestAssured/Appium script. Implements the shared {@code core}
 * {@link Guardrail} seam (ADR-015); wired in via {@link ExecutionValidator}.
 */
@Component
public class ScriptSurfaceGuardrail implements Guardrail {

    /** Unambiguous out-of-surface tokens (case-insensitive substring match). */
    private static final List<String> DENY_SUBSTRINGS = List.of(
            "child_process", "runtime.getruntime", "processbuilder", "os.system", "subprocess",
            "invoke-expression", "invoke-webrequest", "/bin/sh", "/bin/bash", "cmd.exe",
            "rm -rf", "powershell");

    /** Word-boundary patterns to avoid false positives (e.g. {@code eval(} must not match {@code evaluate(}). */
    private static final List<Pattern> DENY_PATTERNS = List.of(
            Pattern.compile("\\beval\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bspawn\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bexecSync\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bcurl\\s", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bwget\\s", Pattern.CASE_INSENSITIVE));

    @Override
    public String getName() {
        return "script-surface";
    }

    @Override
    public GuardrailVerdict check(String content, GuardrailContext context) {
        if (content == null || content.isBlank()) {
            return GuardrailVerdict.allow();
        }
        String lower = content.toLowerCase();
        for (String token : DENY_SUBSTRINGS) {
            if (lower.contains(token)) {
                return GuardrailVerdict.block("out-of-surface call: " + token);
            }
        }
        for (Pattern p : DENY_PATTERNS) {
            if (p.matcher(content).find()) {
                return GuardrailVerdict.block("out-of-surface call: " + p.pattern());
            }
        }
        return GuardrailVerdict.allow();
    }
}
