-- LRN-3 (Option B): persist the learning loop's observations, so LRN-2's metrics have a real source.
--
-- Context: LearningMetricsCalculator could always compute Success Rate / Confidence History /
-- Learning Score / trend, but LearningObservation had no producer and no persisted form -- the
-- read-model would have shown an empty dashboard forever (ADR-063's producerless rule, which is why
-- LRN-3 was deferred in ADR-062/063). The orchestration run pipeline is the one place a run's real
-- pass/fail and its real AI-1 gate confidence exist together; this table is where it writes them.
--
-- Owner: ai-qa-os-gateway (single Flyway owner, ADR-024).

CREATE TABLE learning_observations (
    id           UUID         PRIMARY KEY,
    tenant_id    VARCHAR(64)  NOT NULL DEFAULT '__system__',
    sequence_no  BIGINT       NOT NULL,
    success      BOOLEAN      NOT NULL,
    confidence   DOUBLE PRECISION NOT NULL,
    label        VARCHAR(200),
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    version      BIGINT,
    active       BOOLEAN      DEFAULT TRUE,
    deleted      BOOLEAN      DEFAULT FALSE
);

-- The read path fetches the newest N by sequence_no and the ORM filters by tenant, so index both.
CREATE INDEX ix_learning_observations_tenant   ON learning_observations (tenant_id);
CREATE INDEX ix_learning_observations_sequence ON learning_observations (sequence_no DESC);
