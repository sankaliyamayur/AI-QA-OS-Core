package com.aiqaos.learning.metrics;

import java.util.List;
import java.util.OptionalDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * LRN-3 (Option B): the producer LRN-2 never had. Persists one {@link LearningObservationEntity}
 * per completed run, so the learning dashboard is fed by what actually happened.
 *
 * <p><b>Why this is a real producer.</b> ADR-062/063 deferred LRN-3 because nothing produced
 * observations and {@code brain_learning} lacks the {@code success} + {@code confidence} the metrics
 * need — deriving them there would have fabricated confidence. The orchestration run pipeline is the
 * one place both signals genuinely exist: the run's terminal status, and the per-step confidences the
 * AI-1 gate already evaluates. This records those, and only those.
 *
 * <p><b>Unmeasured runs are skipped, not zero-filled.</b> A step that reports no confidence surfaces
 * {@code 0.0} (documented on {@code WorkflowResponse} as "not reported → UNGATED"). Averaging those
 * in would invent a low confidence nobody measured, and {@code LearningMetricsCalculator} means
 * confidence across every observation — so a run where no step reported one is not recorded at all.
 * The sample is therefore "runs whose confidence was observed", which is honest and documented,
 * rather than "all runs, some with made-up confidence".
 *
 * <p><b>Opt-in and best-effort.</b> Registered only when {@code aiqaos.learning.observations.enabled=true};
 * recording never throws, because a learning datapoint must never fail the pipeline run it describes.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.learning.observations.enabled", havingValue = "true")
public class LearningObservationRecorder {

    private static final Logger log = LoggerFactory.getLogger(LearningObservationRecorder.class);

    private final LearningObservationRepository repository;

    public LearningObservationRecorder(LearningObservationRepository repository) {
        this.repository = repository;
    }

    /**
     * Record one run. {@code reportedConfidences} are the confidences the run's steps actually
     * reported; an empty/blank set means the run's confidence was never measured and nothing is
     * written. Returns true when an observation was persisted.
     *
     * @param success             the run's real terminal outcome
     * @param reportedConfidences confidences observed during the run (values &gt; 0)
     * @param sequenceNo          ordering key — epoch millis of completion
     * @param label               optional descriptor, e.g. the run's correlation id
     */
    public boolean record(boolean success, List<Double> reportedConfidences, long sequenceNo, String label) {
        try {
            OptionalDouble mean = meanOfObserved(reportedConfidences);
            if (mean.isEmpty()) {
                log.debug("[learning-observations] run {} reported no confidence — not recorded", label);
                return false;
            }
            LearningObservationEntity entity = new LearningObservationEntity();
            entity.setSuccess(success);
            entity.setConfidence(mean.getAsDouble());
            entity.setSequenceNo(sequenceNo);
            entity.setLabel(label);
            // tenant_id is stamped by Hibernate's @TenantId discriminator (ADR-054/057).
            repository.save(entity);
            return true;
        } catch (Exception e) {
            log.warn("[learning-observations] failed to record run {}: {}", label, e.getMessage());
            return false;
        }
    }

    /**
     * Mean of the confidences that were genuinely reported. Values of 0 mean "not reported"
     * (WorkflowResponse), and anything outside 0..1 is not a confidence, so both are excluded rather
     * than skewing the mean.
     */
    private static OptionalDouble meanOfObserved(List<Double> confidences) {
        if (confidences == null || confidences.isEmpty()) {
            return OptionalDouble.empty();
        }
        return confidences.stream()
                .filter(c -> c != null && c > 0.0 && c <= 1.0)
                .mapToDouble(Double::doubleValue)
                .average();
    }
}
