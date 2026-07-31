package com.aiqaos.core.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * FI-ENT1-C: bridges the request-bound {@link TenantContextHolder} to Hibernate's discriminator
 * multi-tenancy (ADR-054). Hibernate calls this on every session to learn "which tenant?", then
 * automatically appends {@code WHERE tenant_id = ?} to reads of {@code @TenantId} entities and stamps
 * the column on writes — so isolation is enforced by the ORM, never by remembering to add a clause.
 *
 * <p>When no tenant is bound (platform/background work, or pre-tenancy "legacy" rows) it returns
 * {@link TenantContext#SYSTEM_TENANT}: those operations see system-owned data, which is correct, not a
 * leak. The value is always non-null, as Hibernate requires.
 *
 * <p>Deliberately <b>not</b> a Spring bean — it is instantiated and registered explicitly by
 * {@link TenantHibernateCustomizer}, keeping the wiring deterministic and Spring Boot's own
 * multi-tenancy auto-detection out of the picture.
 */
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContextHolder.current()
                .map(TenantContext::getTenantId)
                .orElse(TenantContext.SYSTEM_TENANT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
