-- HEAL-3 (FI-HEAL3-A): persist observed locator failures, so "which locators drift most" can be
-- answered from something real.
--
-- Context: ADR-070 deferred the drift ranking because the only reuse count lived in HealingMemory
-- over a non-enumerable MemoryStore, and the one enumerable table (healing_metrics) carries no
-- locator identity. ADR-072 then deferred the unblocker itself, because nothing produced a
-- structured broken locator. Playwright's own call log does report one on a locator failure
-- (ADR-094), so this table now has a faithful producer rather than being scaffolding.
--
-- A row is an observed FAILURE, not a heal: healed_to and friends are nullable, because a locator
-- that breaks and cannot be fixed is precisely the one worth ranking.
--
-- Owner: ai-qa-os-gateway (single Flyway owner, ADR-024).

CREATE TABLE locator_drift (
    id             UUID         PRIMARY KEY,
    tenant_id      VARCHAR(64)  NOT NULL DEFAULT '__system__',
    selector       VARCHAR(500) NOT NULL,
    test_case_id   VARCHAR(100),
    failing_action VARCHAR(100),
    provenance     VARCHAR(50)  NOT NULL,
    execution_id   UUID,
    correlation_id VARCHAR(100),
    healed_to      VARCHAR(500),
    heal_strategy  VARCHAR(50),
    heal_approval  VARCHAR(50),
    observed_at    TIMESTAMP    NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT,
    active         BOOLEAN      DEFAULT TRUE,
    deleted        BOOLEAN      DEFAULT FALSE
);

-- The ranking groups by selector and the ORM filters by tenant; recency queries sort by observed_at.
CREATE INDEX ix_locator_drift_selector ON locator_drift (selector);
CREATE INDEX ix_locator_drift_tenant   ON locator_drift (tenant_id);
CREATE INDEX ix_locator_drift_observed ON locator_drift (observed_at DESC);
