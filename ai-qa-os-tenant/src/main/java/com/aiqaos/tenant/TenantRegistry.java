package com.aiqaos.tenant;

import java.util.List;
import java.util.Optional;

/**
 * MOD-1 seam: owns the set of known tenants and their lifecycle. The reference
 * {@link InMemoryTenantRegistry} keeps it fully testable; a durable (JPA) registry is a drop-in impl
 * later (FI-MOD1-A), landing with the persistence-tenancy increment that needs a DB.
 */
public interface TenantRegistry {

    Tenant register(Tenant tenant);

    Optional<Tenant> find(String tenantId);

    List<Tenant> all();

    /** Move a tenant to {@code ACTIVE}. Returns false if the tenant is unknown. */
    boolean activate(String tenantId);

    /** Move a tenant to {@code SUSPENDED}. Returns false if the tenant is unknown. */
    boolean suspend(String tenantId);
}
