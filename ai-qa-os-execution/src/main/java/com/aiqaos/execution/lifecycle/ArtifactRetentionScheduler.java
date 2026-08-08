package com.aiqaos.execution.lifecycle;

import com.aiqaos.core.tenant.TenantContext;
import com.aiqaos.core.tenant.TenantContextHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 * ENT-5 (FI-ENT5-F): the periodic trigger for {@link ArtifactRetentionService}. Until now the purge
 * was on-demand only — nothing in a running app ever called {@code purgeExpired()}, so the artifact
 * tree grew unbounded no matter how retention was configured. This closes ENT-5's "data lifecycle"
 * half at runtime, complementing the out-of-cluster backup CronJobs (FI-ENT5-B / ADR-074).
 *
 * <p><b>Tenant-aware by construction.</b> The stores are tenant-namespaced (ADR-056), and a timer
 * thread has no tenant bound, so a naive sweep would only ever purge {@code __system__} and silently
 * leak every real tenant's artifacts. This binds each tenant from {@link RetentionTenantSource} in
 * turn via the scoped {@link TenantContextHolder#call} helper, leaving the thread unbound afterwards.
 *
 * <p><b>Owns its timer deliberately.</b> It does not use {@code @Scheduled}: the gateway that hosts
 * execution has no {@code @EnableScheduling}, so an annotation-driven trigger would silently never
 * fire, and switching scheduling on globally there would also wake unrelated {@code @Scheduled} beans
 * already on its classpath (notably {@code MemoryCleaner}'s hourly store-wide cache wipe). A private
 * single-thread scheduler keeps the blast radius to this feature alone and works in any app.
 *
 * <p><b>Opt-in and fail-soft.</b> The bean is not registered at all unless
 * {@code aiqaos.artifacts.retention.enabled=true} (the same flag that arms the service), so the
 * default deployment is byte-for-byte unchanged. One tenant's failure never aborts the sweep and no
 * failure escapes the timer thread. Schedule with {@code aiqaos.artifacts.retention.cron} (default:
 * daily at 03:00, off-peak); set it to {@code -} to register the bean without a timer, leaving
 * {@link #purgeAllTenants()} as an on-demand ops entry point.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.artifacts.retention.enabled", havingValue = "true")
public class ArtifactRetentionScheduler {

    /** Mirrors {@code Scheduled.CRON_DISABLED}: register the bean but start no timer. */
    static final String CRON_DISABLED = "-";

    private static final Logger log = LoggerFactory.getLogger(ArtifactRetentionScheduler.class);

    private final ArtifactRetentionService retentionService;
    private final ObjectProvider<RetentionTenantSource> tenantSourceProvider;
    private final String cron;

    private ThreadPoolTaskScheduler taskScheduler;

    public ArtifactRetentionScheduler(ArtifactRetentionService retentionService,
                                      ObjectProvider<RetentionTenantSource> tenantSourceProvider,
                                      @Value("${aiqaos.artifacts.retention.cron:0 0 3 * * *}") String cron) {
        this.retentionService = retentionService;
        this.tenantSourceProvider = tenantSourceProvider;
        this.cron = (cron == null || cron.isBlank()) ? CRON_DISABLED : cron.trim();
    }

    /**
     * Start the timer. An invalid cron expression fails fast here rather than degrading to a
     * retention policy that is configured but never runs — the same contract as {@code @Scheduled}.
     */
    @PostConstruct
    void start() {
        if (CRON_DISABLED.equals(cron)) {
            log.info("[artifact-retention] enabled without a schedule (cron='-') — on-demand only");
            return;
        }
        CronTrigger trigger = new CronTrigger(cron); // throws on a malformed expression
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("artifact-retention-");
        taskScheduler.setDaemon(true);
        taskScheduler.initialize();
        taskScheduler.schedule(this::runScheduledPurge, trigger);
        log.info("[artifact-retention] scheduled sweep armed (cron='{}')", cron);
    }

    @PreDestroy
    void stop() {
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }

    /**
     * The timer entry point. Deliberately swallows everything: an exception thrown back at the
     * scheduler cancels the repeating task, turning a transient storage blip into a permanently
     * disabled retention policy.
     */
    void runScheduledPurge() {
        try {
            purgeAllTenants();
        } catch (Exception e) {
            log.error("[artifact-retention] scheduled sweep failed", e);
        }
    }

    /**
     * Sweep every known tenant's artifact tree. Returns the total number of artifacts purged.
     * Exposed (rather than inlined into the timer task) so the sweep can be unit-proven and
     * triggered on demand by ops.
     */
    public int purgeAllTenants() {
        List<String> tenantIds = resolveTenantIds();
        int total = 0;
        for (String tenantId : tenantIds) {
            total += purgeTenant(tenantId);
        }
        if (total > 0) {
            log.info("[artifact-retention] scheduled sweep purged {} artifact(s) across {} tenant(s)",
                    total, tenantIds.size());
        } else {
            log.debug("[artifact-retention] scheduled sweep found nothing to purge across {} tenant(s)",
                    tenantIds.size());
        }
        return total;
    }

    /** Purge one tenant with its context bound; a failure is contained to that tenant. */
    private int purgeTenant(String tenantId) {
        try {
            return TenantContextHolder.call(contextFor(tenantId), retentionService::purgeExpired);
        } catch (Exception e) {
            log.warn("[artifact-retention] sweep failed for tenant {}: {}", tenantId, e.getMessage());
            return 0;
        }
    }

    /**
     * The tenants to sweep — de-duplicated and blank-free, preserving the source's order. With no
     * {@link RetentionTenantSource} on the classpath (single-tenant deployments, or any app that
     * doesn't wire MOD-1's registry) this is the system tenant alone, which is exactly what the
     * unbound behaviour would have purged.
     */
    private List<String> resolveTenantIds() {
        RetentionTenantSource source = tenantSourceProvider.getIfAvailable();
        if (source == null) {
            return List.of(TenantContext.SYSTEM_TENANT);
        }
        List<String> raw = source.tenantIds();
        if (raw == null || raw.isEmpty()) {
            return List.of(TenantContext.SYSTEM_TENANT);
        }
        List<String> ids = new ArrayList<>(new LinkedHashSet<>(raw));
        ids.removeIf(id -> id == null || id.isBlank());
        return ids.isEmpty() ? List.of(TenantContext.SYSTEM_TENANT) : ids;
    }

    private static TenantContext contextFor(String tenantId) {
        return TenantContext.SYSTEM_TENANT.equals(tenantId)
                ? TenantContext.system()
                : TenantContext.ofTenant(tenantId);
    }
}
