-- Phase 22: Enterprise Dashboard, Observability & Monitoring Platform (backend)

-- 1. workflow_executions has never been created by a prior migration (WorkflowExecutionEntity
--    has had no backing table). Create it now with its existing fields plus the new
--    Phase 22 provenance fields (git/LLM/pipeline/environment/browser/current step).
CREATE TABLE workflow_executions (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    execution_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    result VARCHAR(2000),
    total_steps INT,
    success_steps INT,
    failed_steps INT,
    skipped_steps INT,
    retry_count INT,
    execution_cost DOUBLE PRECISION,
    token_usage BIGINT,
    git_commit VARCHAR(64),
    git_branch VARCHAR(255),
    llm_model VARCHAR(100),
    pipeline_version VARCHAR(50),
    environment VARCHAR(50),
    browser VARCHAR(50),
    current_step VARCHAR(150),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_workflow_executions_workflow_id ON workflow_executions (workflow_id);
CREATE INDEX idx_workflow_executions_execution_id ON workflow_executions (execution_id);
CREATE INDEX idx_workflow_executions_start_time ON workflow_executions (start_time);
CREATE INDEX idx_workflow_executions_git_branch ON workflow_executions (git_branch);
CREATE INDEX idx_workflow_executions_status ON workflow_executions (status);

-- 2. Agent-level metrics (per pipeline-step/agent invocation)
CREATE TABLE agent_metrics (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    workflow_id UUID,
    agent_type VARCHAR(100) NOT NULL,
    operation VARCHAR(255),
    correlation_id VARCHAR(100),
    duration_ms BIGINT NOT NULL,
    tokens_used BIGINT,
    cost DOUBLE PRECISION,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message VARCHAR(2000),
    recorded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_agent_metrics_execution_id ON agent_metrics (execution_id);
CREATE INDEX idx_agent_metrics_agent_type ON agent_metrics (agent_type);

-- 3. Timeline events (ordered per-execution step/event log for detail views)
CREATE TABLE timeline_events (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    workflow_id UUID,
    correlation_id VARCHAR(100),
    sequence_number INT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    step_name VARCHAR(150),
    description VARCHAR(2000),
    status VARCHAR(50),
    duration_ms BIGINT,
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_timeline_events_execution_id ON timeline_events (execution_id);
CREATE INDEX idx_timeline_events_execution_seq ON timeline_events (execution_id, sequence_number);
CREATE INDEX idx_timeline_events_correlation_id ON timeline_events (correlation_id);

-- 4. Bug analytics metrics (per detected bug/failure)
CREATE TABLE bug_metrics (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    workflow_id UUID,
    bug_report_id VARCHAR(100),
    failure_category VARCHAR(150),
    severity VARCHAR(50),
    priority VARCHAR(20),
    root_cause VARCHAR(2000),
    confidence DOUBLE PRECISION,
    auto_detected BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50),
    detected_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_bug_metrics_execution_id ON bug_metrics (execution_id);
CREATE INDEX idx_bug_metrics_failure_category ON bug_metrics (failure_category);

-- 5. Self-healing analytics (durable copy of healing attempts; RecoveryHistoryStore in Redis
--    remains the ephemeral 30-day working store used by the healing engine itself)
CREATE TABLE healing_metrics (
    id UUID PRIMARY KEY,
    healing_id VARCHAR(100) NOT NULL,
    execution_id UUID NOT NULL,
    workflow_id UUID,
    failure_category VARCHAR(150),
    action_type VARCHAR(50),
    healing_strategy VARCHAR(150),
    retry_count INT,
    healing_applied BOOLEAN NOT NULL DEFAULT FALSE,
    retry_successful BOOLEAN NOT NULL DEFAULT FALSE,
    recovery_status VARCHAR(50),
    improvement_score DOUBLE PRECISION,
    applied_fix VARCHAR(2000),
    recorded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_healing_metrics_execution_id ON healing_metrics (execution_id);
CREATE INDEX idx_healing_metrics_action_type ON healing_metrics (action_type);

-- 6. Agent tracing: full prompt/response history per LLM call (enterprise-grade dashboard
--    drill-down). Linked to executions/workflows indirectly via correlation_id, joined
--    through timeline_events.correlation_id -> timeline_events.execution_id.
CREATE TABLE agent_traces (
    id UUID PRIMARY KEY,
    correlation_id VARCHAR(100),
    agent_type VARCHAR(100),
    purpose VARCHAR(255),
    provider VARCHAR(100),
    model VARCHAR(100),
    prompt TEXT,
    response TEXT,
    prompt_tokens BIGINT,
    completion_tokens BIGINT,
    latency_ms BIGINT,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_agent_traces_correlation_id ON agent_traces (correlation_id);
CREATE INDEX idx_agent_traces_timestamp ON agent_traces (timestamp);
