package com.aiqaos.tenant;

/**
 * MOD-1: a tenant's lifecycle status. Only an {@code ACTIVE} tenant may resolve to a usable context;
 * a {@code SUSPENDED} tenant is refused at resolution.
 */
public enum TenantStatus {
    ACTIVE,
    SUSPENDED
}
