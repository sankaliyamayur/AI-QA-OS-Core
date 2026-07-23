-- MOD-3: evaluation results. One row per evaluator run over a golden case.
CREATE TABLE eval_results (
    id               UUID PRIMARY KEY,
    result_id        UUID NOT NULL,
    suite            VARCHAR(255),
    case_id          VARCHAR(255),
    evaluator        VARCHAR(255),
    score            DOUBLE PRECISION,
    passed           BOOLEAN,
    prompt_version   VARCHAR(255),
    agent_type       VARCHAR(255),
    reason           VARCHAR(2000),
    created_time     TIMESTAMP,

    -- BaseEntity audit columns
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    version          BIGINT,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_eval_results_suite ON eval_results (suite);
CREATE INDEX idx_eval_results_case ON eval_results (case_id);
