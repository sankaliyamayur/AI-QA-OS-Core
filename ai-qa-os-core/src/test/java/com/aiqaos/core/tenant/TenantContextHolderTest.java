package com.aiqaos.core.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aiqaos.core.exception.TenantContextException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/** ENT-1: unit tests for tenant-context binding, MDC attribution, and scoped restore. No Mockito. */
class TenantContextHolderTest {

    @AfterEach
    void tidy() {
        TenantContextHolder.clear(); // never leak thread-local across tests
    }

    @Test
    void currentIsEmptyWhenUnbound() {
        assertThat(TenantContextHolder.current()).isEmpty();
    }

    @Test
    void setBindsAndClearUnbinds() {
        TenantContext ctx = TenantContext.of("acme", "checkout");
        TenantContextHolder.set(ctx);
        assertThat(TenantContextHolder.current()).contains(ctx);

        TenantContextHolder.clear();
        assertThat(TenantContextHolder.current()).isEmpty();
    }

    @Test
    void requireThrowsWhenUnboundAndReturnsWhenBound() {
        assertThatThrownBy(TenantContextHolder::require).isInstanceOf(TenantContextException.class);
        TenantContextHolder.set(TenantContext.ofTenant("acme"));
        assertThat(TenantContextHolder.require().getTenantId()).isEqualTo("acme");
    }

    @Test
    void setPushesTenantIntoMdcAndClearRemovesIt() {
        TenantContextHolder.set(TenantContext.of("acme", "checkout"));
        assertThat(MDC.get(TenantContextHolder.MDC_TENANT_ID)).isEqualTo("acme");
        assertThat(MDC.get(TenantContextHolder.MDC_PROJECT_ID)).isEqualTo("checkout");

        TenantContextHolder.clear();
        assertThat(MDC.get(TenantContextHolder.MDC_TENANT_ID)).isNull();
        assertThat(MDC.get(TenantContextHolder.MDC_PROJECT_ID)).isNull();
    }

    @Test
    void runBindsDuringAndClearsAfterWhenNoPrevious() {
        TenantContext ctx = TenantContext.of("acme", "checkout");
        TenantContextHolder.run(ctx, () -> {
            assertThat(TenantContextHolder.current()).contains(ctx);
            assertThat(MDC.get(TenantContextHolder.MDC_TENANT_ID)).isEqualTo("acme");
        });
        assertThat(TenantContextHolder.current()).isEmpty();
        assertThat(MDC.get(TenantContextHolder.MDC_TENANT_ID)).isNull();
    }

    @Test
    void nestedRunRestoresTheOuterContext() {
        TenantContext outer = TenantContext.ofTenant("outer");
        TenantContext inner = TenantContext.ofTenant("inner");
        TenantContextHolder.run(outer, () -> {
            TenantContextHolder.run(inner, () ->
                    assertThat(TenantContextHolder.require().getTenantId()).isEqualTo("inner"));
            // after the nested scope, the outer context is restored
            assertThat(TenantContextHolder.require().getTenantId()).isEqualTo("outer");
        });
        assertThat(TenantContextHolder.current()).isEmpty();
    }

    @Test
    void callReturnsValueUnderContext() {
        String result = TenantContextHolder.call(TenantContext.ofTenant("acme"),
                () -> TenantContextHolder.require().getTenantId());
        assertThat(result).isEqualTo("acme");
        assertThat(TenantContextHolder.current()).isEmpty();
    }

    @Test
    void systemContextIsMarked() {
        assertThat(TenantContext.system().isSystem()).isTrue();
        assertThat(TenantContext.of("acme", null).isSystem()).isFalse();
    }
}
