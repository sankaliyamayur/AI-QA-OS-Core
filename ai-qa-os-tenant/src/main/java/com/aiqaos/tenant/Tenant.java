package com.aiqaos.tenant;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * MOD-1: a registered tenant — its id, display name, the projects it owns, and its
 * {@link TenantStatus}. This is the "Project Manager" identity the platform previously lacked.
 */
public final class Tenant {

    private final String tenantId;
    private final String name;
    private final Set<String> projectIds;
    private TenantStatus status;

    public Tenant(String tenantId, String name, Set<String> projectIds, TenantStatus status) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId is blank");
        }
        this.tenantId = tenantId;
        this.name = name;
        this.projectIds = new LinkedHashSet<>(projectIds != null ? projectIds : Set.of());
        this.status = status != null ? status : TenantStatus.ACTIVE;
    }

    public static Tenant active(String tenantId, String name) {
        return new Tenant(tenantId, name, Set.of(), TenantStatus.ACTIVE);
    }

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    public String getTenantId() { return tenantId; }
    public String getName() { return name; }
    public Set<String> getProjectIds() { return new LinkedHashSet<>(projectIds); }
    public TenantStatus getStatus() { return status; }
    public void setStatus(TenantStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Tenant{" + tenantId + " '" + name + "' [" + status + "] projects=" + projectIds + "}";
    }
}
