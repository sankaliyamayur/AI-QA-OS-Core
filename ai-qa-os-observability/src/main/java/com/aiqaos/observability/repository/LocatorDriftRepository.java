package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.LocatorDriftEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * HEAL-3 (FI-HEAL3-A/B): observed locator failures, and the drift ranking derived from them.
 */
@Repository
public interface LocatorDriftRepository extends JpaRepository<LocatorDriftEntity, UUID> {

    /**
     * Most-drifting locators: how often each selector has been observed failing, worst first.
     *
     * <p>Grouped in the database rather than in memory — this table gains a row per observed locator
     * failure, so loading it all to count in Java would degrade as history accumulates (the same
     * reasoning as FI-PE3-C's paged history). Each row is {@code [selector, failures, heals]}, where
     * {@code heals} counts the observations for which a replacement was actually proposed, so the UI
     * can distinguish "breaks often and self-heals" from "breaks often and nobody can fix it".
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT d.selector,
                   COUNT(d),
                   SUM(CASE WHEN d.healedTo IS NOT NULL THEN 1L ELSE 0L END)
            FROM LocatorDriftEntity d
            GROUP BY d.selector
            ORDER BY COUNT(d) DESC, d.selector ASC
            """)
    List<Object[]> rankByFailureCount(Pageable pageable);

    List<LocatorDriftEntity> findBySelectorOrderByObservedAtDesc(String selector);
}
