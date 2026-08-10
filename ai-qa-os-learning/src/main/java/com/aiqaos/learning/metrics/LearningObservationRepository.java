package com.aiqaos.learning.metrics;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * LRN-3 (Option B): the persisted learning observations feeding LRN-2's calculator.
 */
@Repository
public interface LearningObservationRepository extends JpaRepository<LearningObservationEntity, UUID> {

    /**
     * The most recent observations, newest first. The read path re-sorts these into chronological
     * order before handing them to the calculator, which computes its trend by comparing the first
     * half of the series to the second — order is load-bearing, not cosmetic.
     *
     * <p>Ordering and limiting are done by the database: this table grows by one row per run.
     */
    List<LearningObservationEntity> findAllByOrderBySequenceNoDesc(Pageable pageable);
}
