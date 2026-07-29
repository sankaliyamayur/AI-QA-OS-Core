package com.aiqaos.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.tenant.exception.TenantResolutionException;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** MOD-1: unit tests for resolving a tenant key to a core TenantContext (active-only). No Mockito. */
class TenantResolverTest {

    private TenantResolver resolverWith(Tenant... tenants) {
        TenantRegistry registry = new InMemoryTenantRegistry();
        for (Tenant t : tenants) {
            registry.register(t);
        }
        return new TenantResolver(registry);
    }

    @Test
    void resolvesActiveTenantToContext() {
        TenantResolver resolver = resolverWith(
                new Tenant("acme", "Acme", Set.of("checkout"), TenantStatus.ACTIVE));
        TenantContext ctx = resolver.resolve("acme", "checkout");
        assertThat(ctx.getTenantId()).isEqualTo("acme");
        assertThat(ctx.getProjectId()).isEqualTo("checkout");
    }

    @Test
    void unknownTenantThrows() {
        assertThatThrownBy(() -> resolverWith().resolve("ghost"))
                .isInstanceOf(TenantResolutionException.class)
                .hasMessageContaining("unknown tenant");
    }

    @Test
    void suspendedTenantThrows() {
        TenantResolver resolver = resolverWith(
                new Tenant("acme", "Acme", Set.of(), TenantStatus.SUSPENDED));
        assertThatThrownBy(() -> resolver.resolve("acme"))
                .isInstanceOf(TenantResolutionException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void unownedProjectThrows() {
        TenantResolver resolver = resolverWith(
                new Tenant("acme", "Acme", Set.of("checkout"), TenantStatus.ACTIVE));
        assertThatThrownBy(() -> resolver.resolve("acme", "billing"))
                .isInstanceOf(TenantResolutionException.class)
                .hasMessageContaining("does not own project");
    }
}
