package com.aiqaos.security.config;

import com.aiqaos.security.jwt.JwtAuthenticationFilter;
import com.aiqaos.security.filter.RateLimitingFilter;
import com.aiqaos.security.web.RestAccessDeniedHandler;
import com.aiqaos.security.web.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SEC-1 — Restore real authentication and authorization.
 *
 * <p>Before SEC-1 this class both {@code permitAll()}'d nearly every path AND registered a
 * {@code WebSecurityCustomizer.ignoring()} bean that bypassed the security filter chain entirely,
 * leaving every endpoint on both runnable apps reachable with no credentials.
 *
 * <p>The {@code ignoring()} bypass is removed. Enforcement is deny-by-default: only a small public
 * allow-list is open; everything else requires a valid JWT. Enforcement is gated by
 * {@code aiqaos.security.enabled} so local development and the test suite stay open, while the
 * gateway and dashboard apps (which set the flag {@code true}) enforce.
 *
 * <p>Scope note: role-based authorization is intentionally limited to "authenticated" — the current
 * data model has no user→role relationship, and wiring one is out of SEC-1's scope (frozen roadmap).
 * Transport/CSP headers are hardened via {@link SecurityHeaders} (SEC-4, ADR-016).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Public endpoints (no authentication). Everything not listed here is denied by default when
     * enforcement is enabled. Per the SEC-1 roadmap item: auth endpoints, actuator liveness, and
     * the OpenAPI docs only.
     */
    static final String[] PUBLIC_PATHS = {
            "/api/auth/**",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/error"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    /** SEC-4: overrides the strict default CSP when set; blank keeps {@link SecurityHeaders#STRICT_CSP}. */
    @Value("${aiqaos.security.csp:}")
    private String csp;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RateLimitingFilter rateLimitingFilter,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * Enforced chain (active when {@code aiqaos.security.enabled=true}): deny-by-default.
     * The gateway and dashboard apps set this flag; unset contexts (tests, local) use the
     * permissive chain below.
     */
    @Bean
    @ConditionalOnProperty(name = "aiqaos.security.enabled", havingValue = "true")
    public SecurityFilterChain enforcedFilterChain(HttpSecurity http) throws Exception {
        applyCommonHardening(http);
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)   // 401 JSON
                .accessDeniedHandler(accessDeniedHandler)             // 403 JSON
            );
        return http.build();
    }

    /**
     * Permissive chain (default when the flag is absent or {@code false}): preserves the previous
     * open behaviour for local development and the existing test suite. The JWT filter still runs,
     * so a supplied token is honoured, but no request is rejected.
     */
    @Bean
    @ConditionalOnProperty(name = "aiqaos.security.enabled", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain permissiveFilterChain(HttpSecurity http) throws Exception {
        applyCommonHardening(http);
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * Shared configuration for both chains. Preserves the SEC-1 filter order (rate-limit before the
     * auth filter, JWT filter after it). SEC-4: transport/CSP headers now come from the hardened,
     * configurable {@link SecurityHeaders} policy (strict CSP, {@code X-Frame-Options: DENY},
     * Referrer/Permissions policies) instead of the former {@code default-src *} / disabled frames.
     */
    private void applyCommonHardening(HttpSecurity http) throws Exception {
        SecurityHeaders.apply(http, csp);
        http
            // JWT stateless REST API model: CSRF disabled intentionally
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configure(http))
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtAuthenticationFilter, RateLimitingFilter.class);
    }
}
