package com.aiqaos.core.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * FI-ENT1-C (ADR-054): the resolver that hands Hibernate the current tenant. The security-critical
 * behaviour is that an <em>unbound</em> context resolves to the system tenant (never null, never a
 * leak into an arbitrary tenant), and a bound context resolves to exactly that tenant.
 */
class TenantIdentifierResolverTest {

    private final TenantIdentifierResolver resolver = new TenantIdentifierResolver();

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void unbound_resolvesToSystemTenant() {
        TenantContextHolder.clear();
        assertEquals(TenantContext.SYSTEM_TENANT, resolver.resolveCurrentTenantIdentifier());
    }

    @Test
    void bound_resolvesToThatTenant() {
        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        assertEquals("acme", resolver.resolveCurrentTenantIdentifier());
    }

    @Test
    void afterClear_resolvesBackToSystemTenant() {
        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        TenantContextHolder.clear();
        assertEquals(TenantContext.SYSTEM_TENANT, resolver.resolveCurrentTenantIdentifier());
    }

    @Test
    void systemContext_resolvesToSystemTenant() {
        TenantContextHolder.set(TenantContext.system());
        assertEquals(TenantContext.SYSTEM_TENANT, resolver.resolveCurrentTenantIdentifier());
    }

    @Test
    void validatesExistingSessions() {
        assertTrue(resolver.validateExistingCurrentSessions());
    }
}
