-- Phase 23: fills the gap between JPA entities on the dashboard module's classpath
-- (core, security, observability, orchestration, agents, execution, learning, healing,
-- memory, intelligence, ai-provider) and the tables actually created by V1-V4.
--
-- V1 created workflow_definitions/execution_records, which match neither WorkflowEntity
-- (@Table workflows) nor ExecutionEntity (@Table executions) - those two are left as
-- unused/orphaned local-dev tables (already applied/checksummed by Flyway) and the
-- correctly-named tables are created fresh below instead of renaming in place.

-- 1. Agents module
CREATE TABLE agents (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE agent_executions (
    id UUID PRIMARY KEY,
    agent_id UUID NOT NULL,
    request_payload OID NOT NULL,
    response_payload OID NOT NULL,
    status VARCHAR(50) NOT NULL,
    execution_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_agent_executions_agent_id ON agent_executions (agent_id);

-- 2. Execution module
CREATE TABLE executions (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    workflow_id UUID NOT NULL,
    provider VARCHAR(100) NOT NULL,
    environment VARCHAR(100),
    browser VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_executions_workflow_id ON executions (workflow_id);

CREATE TABLE execution_steps (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    screenshot_url VARCHAR(2000),
    log_content TEXT,
    retry_count INT,
    duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_execution_steps_execution_id ON execution_steps (execution_id);

CREATE TABLE execution_artifacts (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    screenshot_path VARCHAR(2000),
    video_path VARCHAR(2000),
    trace_path VARCHAR(2000),
    log_path VARCHAR(2000),
    report_path VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_execution_artifacts_execution_id ON execution_artifacts (execution_id);

CREATE TABLE execution_audits (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    action_name VARCHAR(255) NOT NULL,
    locator VARCHAR(2000),
    command_details TEXT,
    timestamp TIMESTAMP NOT NULL,
    duration_ms BIGINT,
    exception_message VARCHAR(2000),
    screenshot_ref VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_execution_audits_execution_id ON execution_audits (execution_id);

-- 3. Memory module
CREATE TABLE memory_nodes (
    id UUID PRIMARY KEY,
    content OID NOT NULL,
    memory_type VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    owner_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE memory_node_metadata (
    memory_node_id UUID NOT NULL,
    meta_key VARCHAR(255) NOT NULL,
    meta_value VARCHAR(2000)
);
CREATE INDEX idx_memory_node_metadata_node_id ON memory_node_metadata (memory_node_id);

CREATE TABLE conversation_histories (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    message VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_conversation_histories_session_id ON conversation_histories (session_id);

-- 4. Intelligence (prompt) module
CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(2000),
    active_version VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE prompt_versions (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    version_val VARCHAR(50) NOT NULL,
    content OID NOT NULL,
    author VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_prompt_versions_template_id ON prompt_versions (template_id);

CREATE TABLE prompt_executions (
    id UUID PRIMARY KEY,
    template_id UUID,
    version_id UUID,
    final_compiled_prompt OID NOT NULL,
    trace_id VARCHAR(100),
    response_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_prompt_executions_template_id ON prompt_executions (template_id);

-- 5. Orchestration module (workflows/workflow_steps - distinct from V1's orphaned
--    workflow_definitions, which doesn't match WorkflowEntity's actual mapping)
CREATE TABLE workflows (
    id UUID PRIMARY KEY,
    workflow_name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    workflow_version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE workflow_steps (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    agent_type VARCHAR(100) NOT NULL,
    execution_order INT NOT NULL,
    retry_count INT NOT NULL,
    timeout_seconds BIGINT,
    condition_rule VARCHAR(2000),
    is_parallel BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_workflow_steps_workflow_id ON workflow_steps (workflow_id);

-- 6. Security module (api keys/sessions/password history - pulled onto the dashboard's
--    classpath transitively via ai-provider -> security; users/roles/permissions/audit
--    already exist from V2)
CREATE TABLE security_api_keys (
    id UUID PRIMARY KEY,
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    secret_hash VARCHAR(255) NOT NULL,
    expiry TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE security_api_keys_permissions (
    security_api_keys_id UUID NOT NULL,
    permission VARCHAR(255)
);
CREATE INDEX idx_security_api_keys_permissions_key_id ON security_api_keys_permissions (security_api_keys_id);

CREATE TABLE security_user_sessions (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    device VARCHAR(255),
    browser VARCHAR(100),
    ip_address VARCHAR(100),
    location VARCHAR(255),
    login_time TIMESTAMP NOT NULL,
    last_activity TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    refresh_token VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_security_user_sessions_user_id ON security_user_sessions (user_id);

CREATE TABLE security_password_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    changed_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_security_password_history_user_id ON security_password_history (user_id);
