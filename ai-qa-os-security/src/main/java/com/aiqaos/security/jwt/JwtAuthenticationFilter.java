package com.aiqaos.security.jwt;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.rbac.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.beans.factory.ObjectProvider;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ObjectProvider<UserRepository> userRepositoryProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepositoryProvider.getIfAvailable();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        Claims claims = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                claims = jwtTokenProvider.getClaimsFromToken(token);
            }
        }

        if (claims == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // FI-ENT1-D (ADR-055): for an authenticated request the tenant comes from the SIGNED token,
        // never the spoofable X-Tenant-ID header. Bind it authoritatively for the whole downstream leg
        // so the user load below is tenant-filtered (@TenantId) — a token minted for tenant A cannot
        // resolve a user in tenant B (→ 401). Restore the previous context after (mirrors the gateway
        // filter's discipline); the gateway TenantContextFilter yields to an already-bound tenant.
        String tokenTenant = claims.get("tenantId", String.class);
        TenantContext previous = TenantContextHolder.current().orElse(null);
        TenantContext tenantCtx = (tokenTenant != null && !tokenTenant.isBlank())
                ? TenantContext.ofTenant(tokenTenant) : TenantContext.system();
        TenantContextHolder.set(tenantCtx);
        try {
            authenticate(claims, request);
            filterChain.doFilter(request, response);
        } finally {
            if (previous != null) {
                TenantContextHolder.set(previous);
            } else {
                TenantContextHolder.clear();
            }
        }
    }

    /** Loads the token's principal (tenant-filtered) and binds it into the Spring {@code SecurityContext}. */
    private void authenticate(Claims claims, HttpServletRequest request) {
        String userId = claims.getSubject();
        if (userRepository != null) {
            UserEntity user = userRepository.findById(UUID.fromString(userId)).orElse(null);
            if (user != null && user.isEnabled() && !user.isAccountLocked()) {
                // FI-ENT4-C (ADR-066): authorities derived from the user's persisted roles
                // (ROLE_USER baseline + ROLE_<name> per role) so hasRole('ADMIN') works.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, com.aiqaos.security.authorization.AuthorityMapper.authorities(user)
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            String username = claims.get("username", String.class);
            if (username == null) {
                username = userId;
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
