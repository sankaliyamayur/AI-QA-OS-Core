package com.aiqaos.core.tenant;

import com.aiqaos.core.exception.TenantContextException;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.MDC;

/**
 * ENT-1: binds the current {@link TenantContext} to the executing thread and mirrors it into the
 * {@code MDC} so every log line is tenant-attributed — the same set-and-clear-in-{@code finally}
 * discipline as {@code CorrelationIdFilter}. Prefer the scoped {@link #run}/{@link #call} helpers so
 * the previous context is always restored (nesting-safe) and the MDC cannot leak across threads.
 */
public final class TenantContextHolder {

    public static final String MDC_TENANT_ID = "tenantId";
    public static final String MDC_PROJECT_ID = "projectId";

    private static final ThreadLocal<TenantContext> HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    /** Bind {@code context} to the current thread (and MDC). Prefer {@link #run}/{@link #call}. */
    public static void set(TenantContext context) {
        if (context == null) {
            clear();
            return;
        }
        HOLDER.set(context);
        MDC.put(MDC_TENANT_ID, context.getTenantId());
        if (context.getProjectId() != null) {
            MDC.put(MDC_PROJECT_ID, context.getProjectId());
        } else {
            MDC.remove(MDC_PROJECT_ID);
        }
    }

    /** Unbind the current thread's context (and MDC). */
    public static void clear() {
        HOLDER.remove();
        MDC.remove(MDC_TENANT_ID);
        MDC.remove(MDC_PROJECT_ID);
    }

    /** The context bound to the current thread, if any. */
    public static Optional<TenantContext> current() {
        return Optional.ofNullable(HOLDER.get());
    }

    /** The bound context, or throw {@link TenantContextException} if the thread is unscoped. */
    public static TenantContext require() {
        TenantContext ctx = HOLDER.get();
        if (ctx == null) {
            throw new TenantContextException("no tenant context bound to the current thread");
        }
        return ctx;
    }

    /** Run {@code action} with {@code context} bound, restoring the previous context afterwards. */
    public static void run(TenantContext context, Runnable action) {
        call(context, () -> {
            action.run();
            return null;
        });
    }

    /** Call {@code action} with {@code context} bound, restoring the previous context afterwards. */
    public static <T> T call(TenantContext context, Supplier<T> action) {
        TenantContext previous = HOLDER.get();
        set(context);
        try {
            return action.get();
        } finally {
            if (previous != null) {
                set(previous);
            } else {
                clear();
            }
        }
    }
}
