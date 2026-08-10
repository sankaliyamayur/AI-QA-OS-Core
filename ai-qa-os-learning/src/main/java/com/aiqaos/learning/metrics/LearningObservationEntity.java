package com.aiqaos.learning.metrics;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

/**
 * LRN-3 (Option B): one persisted {@link LearningObservation} — the durable form of a single run's
 * contribution to the learning loop.
 *
 * <p>LRN-2's calculator has always been able to compute metrics; what was missing was a source. The
 * observations it needs (a real pass/fail and a real confidence) converge in exactly one place, the
 * orchestration run pipeline, and nothing was writing them down. This entity is that record.
 *
 * <p><b>Only observed runs are stored.</b> {@code confidence} is the mean of the confidences the
 * run's steps actually reported; a run where no step reported one is not written at all rather than
 * being recorded as zero, which would drag the dashboard's average confidence down with a number
 * nobody measured (ADR-063).
 */
@Entity
@Table(name = "learning_observations")
public class LearningObservationEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C ext (ADR-054/057): tenant-owned data — Hibernate stamps on insert, filters on read.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    /** Ordering key — epoch millis of the run's completion. The calculator needs a time order. */
    @Column(name = "sequence_no", nullable = false)
    private long sequenceNo;

    /** Did the run pass. */
    @Column(name = "success", nullable = false)
    private boolean success;

    /** 0..1 — the mean of the confidences the run's steps actually reported. */
    @Column(name = "confidence", nullable = false)
    private double confidence;

    /** Optional descriptor (the run's correlation id, so an observation traces back to its run). */
    @Column(name = "label", length = 200)
    private String label;

    public long getSequenceNo() { return sequenceNo; }
    public void setSequenceNo(long sequenceNo) { this.sequenceNo = sequenceNo; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    /** Project back onto the LRN-2 contract the calculator consumes. */
    public LearningObservation toObservation() {
        return new LearningObservation(sequenceNo, success, confidence, label);
    }
}
