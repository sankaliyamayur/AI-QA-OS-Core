package com.aiqaos.core.tenant;

/**
 * ENT-1: the contract for tenant-owned data — an entity/record implements this to declare which
 * tenant it belongs to. It is the persistence dimension the enforcement layers (row-level scoping,
 * tenant-scoped retrieval) will key on; enforcement itself is deferred (FI-ENT1-C/E).
 */
public interface Tenanted {

    String getTenantId();
}
