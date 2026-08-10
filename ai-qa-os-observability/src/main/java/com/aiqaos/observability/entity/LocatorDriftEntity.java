package com.aiqaos.observability.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/**
 * HEAL-3 (FI-HEAL3-A): one observed failure of one locator.
 *
 * <p>The enumerable, persisted store whose absence blocked the drift ranking. ADR-070 named the
 * problem exactly: the reuse count lived only in {@code HealingMemory} over a non-enumerable
 * {@code MemoryStore}, and the one enumerable table ({@code healing_metrics}) carries no locator
 * identity — so "which locators drift most" could not be answered from anything real. This table
 * carries the identity.
 *
 * <p><b>A row is a failure, not a heal.</b> Drift is how often a locator broke; whether healing then
 * proposed a replacement is extra information, recorded when it happened. Keying the table on
 * successful heals would undercount exactly the locators that break and cannot be fixed — the ones
 * most worth surfacing.
 */
@Entity
@Table(name = "locator_drift")
public class LocatorDriftEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C ext (ADR-054/057): tenant-owned data — stamped on insert, filtered on read.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    /**
     * The failing selector, verbatim as the test supplied it. This is the grouping key for the drift
     * ranking, so it is never normalised — rewriting it would split one locator into several.
     */
    @Column(name = "selector", length = 500, nullable = false)
    private String selector;

    @Column(name = "test_case_id", length = 100)
    private String testCaseId;

    /** The Playwright call that failed, e.g. {@code page.fill}. */
    @Column(name = "failing_action", length = 100)
    private String failingAction;

    /** Where the selector came from — so a consumer can weigh the signal rather than just trust it. */
    @Column(name = "provenance", length = 50, nullable = false)
    private String provenance;

    @Column(name = "execution_id")
    private UUID executionId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    /** The replacement the healer proposed, when it proposed one. Null means no candidate. */
    @Column(name = "healed_to", length = 500)
    private String healedTo;

    @Column(name = "heal_strategy", length = 50)
    private String healStrategy;

    /** HEAL-2's governance outcome for the proposal — e.g. AUTO_APPROVED, PENDING_APPROVAL. */
    @Column(name = "heal_approval", length = 50)
    private String healApproval;

    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;

    public String getSelector() { return selector; }
    public void setSelector(String selector) { this.selector = selector; }

    public String getTestCaseId() { return testCaseId; }
    public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }

    public String getFailingAction() { return failingAction; }
    public void setFailingAction(String failingAction) { this.failingAction = failingAction; }

    public String getProvenance() { return provenance; }
    public void setProvenance(String provenance) { this.provenance = provenance; }

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getHealedTo() { return healedTo; }
    public void setHealedTo(String healedTo) { this.healedTo = healedTo; }

    public String getHealStrategy() { return healStrategy; }
    public void setHealStrategy(String healStrategy) { this.healStrategy = healStrategy; }

    public String getHealApproval() { return healApproval; }
    public void setHealApproval(String healApproval) { this.healApproval = healApproval; }

    public LocalDateTime getObservedAt() { return observedAt; }
    public void setObservedAt(LocalDateTime observedAt) { this.observedAt = observedAt; }
}
