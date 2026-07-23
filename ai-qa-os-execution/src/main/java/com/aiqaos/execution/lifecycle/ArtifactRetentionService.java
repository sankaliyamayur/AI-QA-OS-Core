package com.aiqaos.execution.lifecycle;

import com.aiqaos.execution.artifact.ArtifactStore;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ENT-5 data-lifecycle: age-based purge of the artifact tree, which otherwise grows unbounded.
 * <b>Opt-in</b> — disabled by default ({@code aiqaos.artifacts.retention.enabled=false}), so it
 * never deletes anything unless explicitly switched on. On-demand ({@link #purgeExpired()}); an
 * ops trigger (a CronJob or a future {@code @Scheduled}) can call it periodically.
 */
@Service
public class ArtifactRetentionService {

    private static final Logger log = LoggerFactory.getLogger(ArtifactRetentionService.class);

    private final ArtifactStore artifactStore;
    private final boolean enabled;
    private final int maxAgeDays;
    private final String prefix;

    public ArtifactRetentionService(ArtifactStore artifactStore,
                                    @Value("${aiqaos.artifacts.retention.enabled:false}") boolean enabled,
                                    @Value("${aiqaos.artifacts.retention.max-age-days:30}") int maxAgeDays,
                                    @Value("${aiqaos.artifacts.retention.prefix:}") String prefix) {
        this.artifactStore = artifactStore;
        this.enabled = enabled;
        this.maxAgeDays = maxAgeDays;
        this.prefix = prefix;
    }

    /**
     * Delete artifacts older than the retention window. Returns the number deleted; a no-op
     * (returns 0) when retention is disabled.
     */
    public int purgeExpired() {
        if (!enabled) {
            return 0;
        }
        Instant cutoff = Instant.now().minus(Duration.ofDays(maxAgeDays));
        int deleted = 0;
        for (String key : artifactStore.list(prefix)) {
            try {
                if (artifactStore.lastModified(key).isBefore(cutoff)) {
                    artifactStore.delete(key);
                    deleted++;
                }
            } catch (Exception e) {
                log.warn("[artifact-retention] skipped {}: {}", key, e.getMessage());
            }
        }
        if (deleted > 0) {
            log.info("[artifact-retention] purged {} artifact(s) older than {} day(s)", deleted, maxAgeDays);
        }
        return deleted;
    }
}
