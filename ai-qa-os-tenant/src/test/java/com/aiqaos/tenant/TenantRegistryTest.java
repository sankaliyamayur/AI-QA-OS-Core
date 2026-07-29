package com.aiqaos.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** MOD-1: unit tests for the in-memory tenant registry — register, lookup, lifecycle. No Mockito. */
class TenantRegistryTest {

    private final TenantRegistry registry = new InMemoryTenantRegistry();

    @Test
    void registersAndFindsATenant() {
        registry.register(new Tenant("acme", "Acme Corp", Set.of("checkout"), TenantStatus.ACTIVE));
        assertThat(registry.find("acme")).isPresent();
        assertThat(registry.find("acme").get().getName()).isEqualTo("Acme Corp");
        assertThat(registry.all()).hasSize(1);
    }

    @Test
    void rejectsDuplicateTenantId() {
        registry.register(Tenant.active("acme", "Acme"));
        assertThatThrownBy(() -> registry.register(Tenant.active("acme", "Acme 2")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void suspendAndActivateChangeStatus() {
        registry.register(Tenant.active("acme", "Acme"));
        assertThat(registry.suspend("acme")).isTrue();
        assertThat(registry.find("acme").get().isActive()).isFalse();
        assertThat(registry.activate("acme")).isTrue();
        assertThat(registry.find("acme").get().isActive()).isTrue();
    }

    @Test
    void lifecycleOnUnknownTenantReturnsFalse() {
        assertThat(registry.suspend("nope")).isFalse();
        assertThat(registry.activate("nope")).isFalse();
    }

    @Test
    void findUnknownIsEmpty() {
        assertThat(registry.find("ghost")).isEmpty();
    }
}
