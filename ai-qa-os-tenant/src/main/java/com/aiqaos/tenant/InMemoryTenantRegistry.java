package com.aiqaos.tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * MOD-1: in-memory reference {@link TenantRegistry} — the default, and the store the resolver is
 * unit-tested against. When a durable registry is added (FI-MOD1-A) it takes precedence via
 * {@code @Primary}/property gating.
 */
@Component
public class InMemoryTenantRegistry implements TenantRegistry {

    private final Map<String, Tenant> byId = new ConcurrentHashMap<>();

    @Override
    public Tenant register(Tenant tenant) {
        if (byId.putIfAbsent(tenant.getTenantId(), tenant) != null) {
            throw new IllegalArgumentException("duplicate tenantId: " + tenant.getTenantId());
        }
        return tenant;
    }

    @Override
    public Optional<Tenant> find(String tenantId) {
        return Optional.ofNullable(byId.get(tenantId));
    }

    @Override
    public List<Tenant> all() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public boolean activate(String tenantId) {
        return setStatus(tenantId, TenantStatus.ACTIVE);
    }

    @Override
    public boolean suspend(String tenantId) {
        return setStatus(tenantId, TenantStatus.SUSPENDED);
    }

    private boolean setStatus(String tenantId, TenantStatus status) {
        Tenant t = byId.get(tenantId);
        if (t == null) {
            return false;
        }
        t.setStatus(status);
        return true;
    }
}
