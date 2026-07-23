package com.aiqaos.dashboard.config;

import com.aiqaos.security.config.SecurityHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Dashboard-specific security configuration.
 *
 * Configures a high-priority filter chain that permits all dashboard API and
 * artifact-serving endpoints without JWT or rate-limiting filters. This is
 * necessary because the shared SecurityConfig adds those filters which would
 * block requests even when 'permitAll()' is set.
 *
 * By setting @Order(1) this chain is evaluated BEFORE the default chain in
 * SecurityConfig (which has no @Order and gets the default lowest precedence).
 */
@Configuration
public class DashboardSecurityConfig {

    /** SEC-4: overrides the strict default CSP when set; blank keeps {@link SecurityHeaders#STRICT_CSP}. */
    @Value("${aiqaos.security.csp:}")
    private String csp;

    /**
     * High-priority security filter chain that permits dashboard and artifact
     * endpoints. Does NOT add JWT or rate-limiting filters, so no token is needed.
     *
     * <p>SEC-4: this chain serves {@code /api/artifacts/**} — the XSS/clickjacking surface — so it
     * now emits the hardened {@link SecurityHeaders} policy, which it previously lacked entirely.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain dashboardFilterChain(HttpSecurity http) throws Exception {
        SecurityHeaders.apply(http, csp);
        http
            .securityMatcher(
                "/api/dashboard/**",
                "/api/artifacts/**",
                "/api/auth/**",
                "/api/v1/**",
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            )
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * CORS configuration allowing the React dev server (port 3000/5173) to
     * call the dashboard backend (port 8090).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
