package com.aiqaos.gateway.lifecycle;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.execution.lifecycle.RetentionTenantSource;
import com.aiqaos.tenant.Tenant;
import com.aiqaos.tenant.TenantRegistry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ENT-5 (FI-ENT5-F): bridges MOD-1's {@link TenantRegistry} to the execution module's
 * {@link RetentionTenantSource} seam, so the scheduled artifact purge actually reaches every
 * registered tenant's namespaced tree instead of only {@code __system__}.
 *
 * <p>Lives in the gateway because it is the one module depending on both {@code ai-qa-os-tenant} and
 * {@code ai-qa-os-execution} — keeping the execution module free of a tenant-registry dependency.
 *
 * <p><b>Suspended tenants are included on purpose.</b> Retention is an age-based storage-lifecycle
 * policy, not an access-control decision; skipping suspended tenants would reintroduce exactly the
 * unbounded-growth leak this closes, and their artifacts are the likeliest to be stale. The system
 * tenant is always swept first — it owns platform-internal and pre-tenancy (backfilled) artifacts.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.artifacts.retention.enabled", havingValue = "true")
public class TenantRegistryRetentionTenantSource implements RetentionTenantSource {

    private final TenantRegistry registry;

    public TenantRegistryRetentionTenantSource(TenantRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<String> tenantIds() {
        List<String> ids = new ArrayList<>();
        ids.add(TenantContext.SYSTEM_TENANT);
        for (Tenant tenant : registry.all()) {
            ids.add(tenant.getTenantId());
        }
        return ids; // de-duplicated by the scheduler
    }
}
