package com.aiqaos.gateway.filter;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.tenant.TenantResolver;
import com.aiqaos.tenant.exception.TenantResolutionException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * ENT-1 FI-ENT1-B: binds the request's tenant to the executing thread for the duration of the call —
 * the runtime half of multi-tenancy. Reads {@code X-Tenant-ID} (+ optional {@code X-Project-ID}),
 * resolves it to a {@code core} {@link TenantContext} via MOD-1's {@link TenantResolver}, and binds it
 * through {@link TenantContextHolder} with a {@code finally}-clear — the same discipline as
 * {@code CorrelationIdFilter} (which runs first, {@code @Order(1)}).
 *
 * <p>No tenant header → the request proceeds unbound (tenancy is optional at this layer; code that
 * requires a tenant calls {@code TenantContextHolder.require()}). An unknown/suspended tenant is
 * rejected with {@code 400} before the chain runs — a bad tenant never reaches downstream handlers.
 */
@Component
@Order(2)
public class TenantContextFilter implements Filter {

    static final String TENANT_HEADER = "X-Tenant-ID";
    static final String PROJECT_HEADER = "X-Project-ID";

    private final TenantResolver resolver;

    public TenantContextFilter(TenantResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // FI-ENT1-D: if a tenant is already bound (the authenticated request's JWT tenant, set by
        // JwtAuthenticationFilter — authoritative and trusted), honour it and ignore the untrusted
        // X-Tenant-ID header. This keeps tenant resolution correct regardless of filter ordering.
        if (TenantContextHolder.current().isPresent()) {
            chain.doFilter(request, response);
            return;
        }

        String tenantId = httpReq.getHeader(TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            chain.doFilter(request, response); // no tenant → proceed unbound
            return;
        }

        TenantContext context;
        try {
            context = resolver.resolve(tenantId, emptyToNull(httpReq.getHeader(PROJECT_HEADER)));
        } catch (TenantResolutionException ex) {
            httpResp.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return; // bad tenant never reaches downstream
        }

        try {
            TenantContextHolder.set(context);
            chain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
