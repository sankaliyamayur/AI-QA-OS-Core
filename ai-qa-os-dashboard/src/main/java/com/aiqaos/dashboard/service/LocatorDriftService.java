package com.aiqaos.dashboard.service;

import com.aiqaos.dashboard.dto.LocatorDriftEntry;
import com.aiqaos.observability.repository.LocatorDriftRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * HEAL-3 (FI-HEAL3-B): the most-drifting-locators ranking.
 *
 * <p>Deferred twice — ADR-070 because no enumerable source carried locator identity, then ADR-072
 * because the unblocker was itself blocked. Both are resolved: {@code locator_drift} is enumerable
 * and has a real producer (ADR-094), so this is now a group-by over observed failures rather than
 * the "trivial read-model over data that does not exist" it would have been.
 *
 * <p>Grouping and limiting are pushed to the database — the table gains a row per observed locator
 * failure, so counting in memory would degrade as history accumulates.
 */
@Service
public class LocatorDriftService {

    private final LocatorDriftRepository repository;
    private final int defaultLimit;
    private final int maxLimit;

    public LocatorDriftService(LocatorDriftRepository repository,
                               @Value("${aiqaos.healing.locator-drift.default-limit:20}") int defaultLimit,
                               @Value("${aiqaos.healing.locator-drift.max-limit:200}") int maxLimit) {
        this.repository = repository;
        this.defaultLimit = defaultLimit;
        this.maxLimit = maxLimit;
    }

    /** Worst-drifting locators first. Empty when nothing has been observed — never fabricated. */
    public List<LocatorDriftEntry> topDrifting(Integer limit) {
        return repository.rankByFailureCount(PageRequest.of(0, clamp(limit))).stream()
                .map(row -> LocatorDriftEntry.of(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        row[2] == null ? 0L : ((Number) row[2]).longValue()))
                .toList();
    }

    private int clamp(Integer limit) {
        int requested = (limit == null || limit <= 0) ? defaultLimit : limit;
        return Math.min(requested, Math.max(1, maxLimit));
    }
}
