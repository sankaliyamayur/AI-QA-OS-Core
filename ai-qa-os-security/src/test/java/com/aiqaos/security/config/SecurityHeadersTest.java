package com.aiqaos.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Guards the SEC-4 hardened CSP invariant: the strict policy must keep the locked-down directives
 * and must not contain the XSS/clickjacking vectors the roadmap flagged.
 */
class SecurityHeadersTest {

    @Test
    void strictCspLocksDownTheDangerousDirectives() {
        String csp = SecurityHeaders.STRICT_CSP;
        assertThat(csp).contains("default-src 'self'");
        assertThat(csp).contains("script-src 'self'");
        assertThat(csp).contains("object-src 'none'");
        assertThat(csp).contains("base-uri 'self'");
        assertThat(csp).contains("frame-ancestors 'none'");
    }

    @Test
    void strictCspRemovesTheKnownXssVectors() {
        String csp = SecurityHeaders.STRICT_CSP;
        // The pre-SEC-4 policy was: default-src * 'unsafe-inline' 'unsafe-eval' data: blob:
        assertThat(csp).doesNotContain("'unsafe-eval'");
        assertThat(csp).doesNotContain("default-src *");
        // scripts must not allow inline (style-src may, which is acceptable / needed by tooling)
        assertThat(csp).doesNotContain("script-src 'self' 'unsafe-inline'");
    }
}
