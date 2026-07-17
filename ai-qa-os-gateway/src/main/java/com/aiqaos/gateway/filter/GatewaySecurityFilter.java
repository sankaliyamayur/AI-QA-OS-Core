package com.aiqaos.gateway.filter;

import com.aiqaos.security.authentication.AuthenticationManager;
import com.aiqaos.security.context.SecurityContext;
import com.aiqaos.security.context.SecurityContextHolder;
import com.aiqaos.security.ratelimit.RateLimiter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(2)
public class GatewaySecurityFilter implements Filter {

    private final AuthenticationManager authenticationManager;
    private final RateLimiter           rateLimiter;

    public GatewaySecurityFilter(AuthenticationManager authenticationManager,
                                  RateLimiter rateLimiter) {
        this.authenticationManager = authenticationManager;
        this.rateLimiter           = rateLimiter;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();

        // Skip auth for public paths
        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpReq.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                       ? authHeader.substring(7) : null;

        SecurityContext ctx = authenticationManager.authenticate(token);
        SecurityContextHolder.setContext(ctx);

        // Rate limiting
        String userId = (ctx != null && ctx.getUserId() != null) ? ctx.getUserId() : "anonymous";
        if (!rateLimiter.isAllowed(userId)) {
            httpResp.setStatus(429);
            httpResp.getWriter().write("{\"error\":\"RATE_LIMIT_EXCEEDED\"}");
            return;
        }

        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isPublic(String path) {
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api/v1") ||
               path.startsWith("/api/dashboard");
    }
}