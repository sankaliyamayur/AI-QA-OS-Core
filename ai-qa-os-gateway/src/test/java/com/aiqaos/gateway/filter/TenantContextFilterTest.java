package com.aiqaos.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.tenant.InMemoryTenantRegistry;
import com.aiqaos.tenant.Tenant;
import com.aiqaos.tenant.TenantRegistry;
import com.aiqaos.tenant.TenantResolver;
import com.aiqaos.tenant.TenantStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * ENT-1 FI-ENT1-B: unit tests for the gateway tenant filter — binds a valid tenant for the request,
 * proceeds unbound when absent, rejects a bad tenant with 400, and always clears afterwards. Uses
 * Spring's MockHttpServletRequest/Response and a capturing chain. No Mockito.
 */
class TenantContextFilterTest {

    /** Captures the tenant bound while the chain runs, and whether it ran. */
    private static final class CapturingChain implements FilterChain {
        boolean called;
        TenantContext boundDuringChain;
        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            called = true;
            boundDuringChain = TenantContextHolder.current().orElse(null);
        }
    }

    private TenantContextFilter filterWith(Tenant... tenants) {
        TenantRegistry registry = new InMemoryTenantRegistry();
        for (Tenant t : tenants) {
            registry.register(t);
        }
        return new TenantContextFilter(new TenantResolver(registry));
    }

    @AfterEach
    void tidy() {
        TenantContextHolder.clear();
    }

    @Test
    void bindsResolvedTenantForTheRequestThenClears() throws Exception {
        TenantContextFilter filter = filterWith(
                new Tenant("acme", "Acme", Set.of("checkout"), TenantStatus.ACTIVE));
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Tenant-ID", "acme");
        req.addHeader("X-Project-ID", "checkout");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        CapturingChain chain = new CapturingChain();

        filter.doFilter(req, resp, chain);

        assertThat(chain.called).isTrue();
        assertThat(chain.boundDuringChain).isNotNull();
        assertThat(chain.boundDuringChain.getTenantId()).isEqualTo("acme");
        assertThat(chain.boundDuringChain.getProjectId()).isEqualTo("checkout");
        assertThat(TenantContextHolder.current()).as("cleared after request").isEmpty();
    }

    @Test
    void proceedsUnboundWhenNoTenantHeader() throws Exception {
        TenantContextFilter filter = filterWith();
        CapturingChain chain = new CapturingChain();

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(chain.called).isTrue();
        assertThat(chain.boundDuringChain).isNull();
    }

    @Test
    void rejectsUnknownTenantWith400AndDoesNotProceed() throws Exception {
        TenantContextFilter filter = filterWith(); // empty registry
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Tenant-ID", "ghost");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        CapturingChain chain = new CapturingChain();

        filter.doFilter(req, resp, chain);

        assertThat(resp.getStatus()).isEqualTo(400);
        assertThat(chain.called).as("bad tenant never reaches downstream").isFalse();
        assertThat(TenantContextHolder.current()).isEmpty();
    }

    @Test
    void rejectsSuspendedTenantWith400() throws Exception {
        TenantContextFilter filter = filterWith(
                new Tenant("acme", "Acme", Set.of(), TenantStatus.SUSPENDED));
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Tenant-ID", "acme");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        CapturingChain chain = new CapturingChain();

        filter.doFilter(req, resp, chain);

        assertThat(resp.getStatus()).isEqualTo(400);
        assertThat(chain.called).isFalse();
    }
}
