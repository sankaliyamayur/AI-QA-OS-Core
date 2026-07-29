package com.aiqaos.tenant;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.tenant.exception.TenantResolutionException;
import org.springframework.stereotype.Service;

/**
 * MOD-1: turns a tenant key into a {@code core} {@link TenantContext} — the bridge between the tenant
 * module and the platform-wide context contract. Resolution succeeds only for a <b>known, active</b>
 * tenant (and, when supplied, a project the tenant owns); otherwise it throws
 * {@link TenantResolutionException}. This is what a gateway tenant-resolution filter will call per
 * request (deferred, FI-ENT1-B), binding the result via {@code TenantContextHolder}.
 */
@Service
public class TenantResolver {

    private final TenantRegistry registry;

    public TenantResolver(TenantRegistry registry) {
        this.registry = registry;
    }

    /** Resolve a tenant (no specific project). */
    public TenantContext resolve(String tenantId) {
        return resolve(tenantId, null);
    }

    /**
     * Resolve {@code tenantId} (and optional {@code projectId}) to a bound-ready {@link TenantContext}.
     * Fails if the tenant is unknown, suspended, or does not own the requested project.
     */
    public TenantContext resolve(String tenantId, String projectId) {
        Tenant tenant = registry.find(tenantId)
                .orElseThrow(() -> new TenantResolutionException("unknown tenant: " + tenantId));
        if (!tenant.isActive()) {
            throw new TenantResolutionException("tenant is not active: " + tenantId);
        }
        if (projectId != null && !tenant.getProjectIds().contains(projectId)) {
            throw new TenantResolutionException(
                    "tenant '" + tenantId + "' does not own project '" + projectId + "'");
        }
        return TenantContext.of(tenantId, projectId);
    }
}
