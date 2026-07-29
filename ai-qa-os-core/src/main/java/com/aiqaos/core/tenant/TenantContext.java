package com.aiqaos.core.tenant;

import java.io.Serializable;
import java.util.Objects;

/**
 * ENT-1: the immutable tenant/project identity that scopes a unit of work. Lives in {@code core} so
 * every module can reference the tenancy dimension without a dependency cycle (the roadmap's "context
 * contract in core"). {@link #system()} is the platform-internal context for non-tenant operations.
 */
public final class TenantContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Reserved tenant id for platform-internal (non-tenant-scoped) work. */
    public static final String SYSTEM_TENANT = "__system__";

    private static final TenantContext SYSTEM = new TenantContext(SYSTEM_TENANT, SYSTEM_TENANT);

    private final String tenantId;
    private final String projectId;

    private TenantContext(String tenantId, String projectId) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.projectId = projectId;
    }

    public static TenantContext of(String tenantId, String projectId) {
        return new TenantContext(tenantId, projectId);
    }

    public static TenantContext ofTenant(String tenantId) {
        return new TenantContext(tenantId, null);
    }

    /** The platform-internal context for work not owned by any tenant. */
    public static TenantContext system() {
        return SYSTEM;
    }

    public boolean isSystem() {
        return SYSTEM_TENANT.equals(tenantId);
    }

    public String getTenantId() { return tenantId; }
    public String getProjectId() { return projectId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantContext that)) return false;
        return tenantId.equals(that.tenantId) && Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, projectId);
    }

    @Override
    public String toString() {
        return "TenantContext{tenant=" + tenantId + ", project=" + projectId + "}";
    }
}
