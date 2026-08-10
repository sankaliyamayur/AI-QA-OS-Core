package com.aiqaos.dashboard.service;

import com.aiqaos.learning.dashboard.LearningDashboardAssembler;
import com.aiqaos.learning.dashboard.LearningDashboardView;
import com.aiqaos.learning.metrics.LearningMetricsCalculator;
import com.aiqaos.learning.metrics.LearningObservation;
import com.aiqaos.learning.metrics.LearningObservationEntity;
import com.aiqaos.learning.metrics.LearningObservationRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * LRN-3 (FI-LRN3-A): the read path that finally connects the learning loop to the dashboard —
 * {@code learning_observations} → LRN-2's {@link LearningMetricsCalculator} → LRN-3's
 * {@link LearningDashboardAssembler}.
 *
 * <p>This was the last missing piece. The assembler and calculator have been built and unit-tested
 * since 2026-07-29, but LRN-3 stayed deferred because nothing produced observations (ADR-062/063's
 * rule against producerless read-models). {@code LearningObservationRecorder} + {@code V24} closed
 * that gap on the orchestration run pipeline, so the read-model now has real data to serve.
 *
 * <p><b>Order is load-bearing.</b> The repository returns the newest observations first (the DB does
 * the ordering and limiting — this table gains a row per run), but the calculator derives its trend
 * by comparing the first half of the series to the second. Handing it newest-first would invert every
 * trend: an improving loop would read as REGRESSING. The window is therefore reversed into
 * chronological order before it is computed.
 */
@Service
public class LearningDashboardService {

    private final LearningObservationRepository repository;
    private final LearningMetricsCalculator calculator;
    private final LearningDashboardAssembler assembler;
    private final int defaultLimit;
    private final int maxLimit;

    public LearningDashboardService(LearningObservationRepository repository,
                                    LearningMetricsCalculator calculator,
                                    LearningDashboardAssembler assembler,
                                    @Value("${aiqaos.learning.dashboard.default-limit:200}") int defaultLimit,
                                    @Value("${aiqaos.learning.dashboard.max-limit:2000}") int maxLimit) {
        this.repository = repository;
        this.calculator = calculator;
        this.assembler = assembler;
        this.defaultLimit = defaultLimit;
        this.maxLimit = maxLimit;
    }

    /**
     * The learning loop's health over the most recent {@code limit} observed runs.
     *
     * @param limit window size; null/non-positive uses the configured default, and any request is
     *              clamped to the configured maximum so a caller cannot ask for the whole table
     */
    public LearningDashboardView getView(Integer limit) {
        List<LearningObservationEntity> newestFirst =
                repository.findAllByOrderBySequenceNoDesc(PageRequest.of(0, clamp(limit)));

        if (newestFirst.isEmpty()) {
            // No runs observed yet. Computing over an empty series would surface a 0.00 score and an
            // AT_RISK verdict on a loop nobody has measured — the assembler's null branch says "no
            // data yet" instead, which is the honest answer (ADR-063).
            return assembler.assemble(null);
        }

        return assembler.assemble(calculator.compute(chronological(newestFirst)));
    }

    /** Reverse the newest-first window the DB returned back into run order. */
    private List<LearningObservation> chronological(List<LearningObservationEntity> newestFirst) {
        List<LearningObservation> ordered = new ArrayList<>(newestFirst.size());
        for (int i = newestFirst.size() - 1; i >= 0; i--) {
            ordered.add(newestFirst.get(i).toObservation());
        }
        return ordered;
    }

    private int clamp(Integer limit) {
        int requested = (limit == null || limit <= 0) ? defaultLimit : limit;
        return Math.min(requested, Math.max(1, maxLimit));
    }
}
