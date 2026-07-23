-- AI-2: human-in-the-loop approval queue. One row per HUMAN_REVIEW pause; updated on decision.
CREATE TABLE human_reviews (
    id               UUID PRIMARY KEY,
    review_id        UUID NOT NULL,
    workflow_id      UUID NOT NULL,
    execution_id     UUID,
    step_name        VARCHAR(255),
    confidence       DOUBLE PRECISION,
    status           VARCHAR(32) NOT NULL,   -- PENDING | APPROVED | REJECTED
    reviewer         VARCHAR(255),
    decision_comment VARCHAR(1000),
    created_time     TIMESTAMP,
    decided_time     TIMESTAMP,

    -- BaseEntity audit columns
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    version          BIGINT,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_human_reviews_status ON human_reviews (status);
CREATE INDEX idx_human_reviews_workflow ON human_reviews (workflow_id);
