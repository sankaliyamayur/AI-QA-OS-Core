package com.aiqaos.security.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

/**
 * SEC-4 — the single source of truth for hardened HTTP response-security headers, applied by both
 * filter chains ({@code SecurityConfig} and {@code DashboardSecurityConfig}). Replaces the previous
 * effectively-absent CSP ({@code default-src * 'unsafe-inline' 'unsafe-eval'}) and disabled
 * frame-options. Recorded as ADR-016.
 */
public final class SecurityHeaders {

    /**
     * Strict default Content-Security-Policy (SEC-4 §0.3a, Option A). No {@code 'unsafe-inline'} on
     * scripts and no {@code 'unsafe-eval'} — the actual XSS vectors. {@code 'unsafe-inline'} is kept
     * only for styles (low risk, needed by Swagger UI / tooling). Tunable via {@code aiqaos.security.csp}.
     */
    public static final String STRICT_CSP =
            "default-src 'self'; "
            + "script-src 'self'; "
            + "style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data:; "
            + "font-src 'self' data:; "
            + "connect-src 'self'; "
            + "object-src 'none'; "
            + "base-uri 'self'; "
            + "form-action 'self'; "
            + "frame-ancestors 'none'";

    private static final String PERMISSIONS_POLICY = "geolocation=(), microphone=(), camera=()";

    private SecurityHeaders() {
    }

    /**
     * Apply the hardened header set to {@code http}. {@code csp} overrides {@link #STRICT_CSP} when
     * non-blank (so ops can tune the policy per environment without a rebuild).
     */
    public static void apply(HttpSecurity http, String csp) throws Exception {
        String policy = (csp == null || csp.isBlank()) ? STRICT_CSP : csp;
        http.headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .contentSecurityPolicy(c -> c.policyDirectives(policy))
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(rp -> rp.policy(ReferrerPolicy.NO_REFERRER))
                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", PERMISSIONS_POLICY)));
        // X-Content-Type-Options: nosniff is emitted by Spring Security's default header writers.
    }
}
