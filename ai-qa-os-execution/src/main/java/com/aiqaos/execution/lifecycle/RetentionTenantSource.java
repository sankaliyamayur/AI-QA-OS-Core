package com.aiqaos.execution.lifecycle;

import java.util.List;

/**
 * ENT-5 (FI-ENT5-F) seam: the tenants whose artifact trees the scheduled purge must sweep.
 *
 * <p>This exists because the {@code ArtifactStore} implementations are <b>tenant-namespaced</b>
 * (ENT-1 / FI-ENT1-E, ADR-056): {@code list}/{@code delete} only ever see the keys under the tenant
 * bound to the calling thread. A background sweep runs on a scheduler thread with <b>no</b> tenant
 * bound, so it would silently resolve to {@code __system__} and leave every real tenant's artifacts
 * growing unbounded — the exact leak retention exists to prevent. The scheduler therefore binds each
 * tenant in turn, and this seam says which ones.
 *
 * <p>{@code ai-qa-os-execution} deliberately does not depend on {@code ai-qa-os-tenant} (MOD-1's
 * registry), so the enumeration is a seam here and the registry-backed adapter lives in the gateway,
 * which already depends on both. With no implementation present the scheduler sweeps the system
 * tenant only — the correct single-tenant behaviour, and never a fabricated tenant list.
 */
public interface RetentionTenantSource {

    /**
     * The tenant ids to sweep. Ids are de-duplicated by the caller, and blank entries ignored; an
     * empty/null result falls back to the system tenant.
     */
    List<String> tenantIds();
}
