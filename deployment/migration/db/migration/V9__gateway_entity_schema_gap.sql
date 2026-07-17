-- ai-qa-os-gateway scans the same com.aiqaos entity closure as ai-qa-os-dashboard, plus three
-- extra modules dashboard does not depend on: ai-qa-os-brain, ai-qa-os-agents-runtime, and
-- ai-qa-os-reporting. Now that gateway shares the dashboard's PostgreSQL database with
-- ddl-auto: validate, these modules' tables need to exist too.

-- 1. Brain module
CREATE TABLE brain_decisions (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    user_input VARCHAR(1000) NOT NULL,
    decision VARCHAR(255) NOT NULL,
    confidence DOUBLE PRECISION,
    timestamp TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_brain_decisions_decision_id ON brain_decisions (decision_id);

CREATE TABLE brain_learning (
    id UUID PRIMARY KEY,
    pattern_name VARCHAR(255) NOT NULL,
    previous_decision VARCHAR(255),
    result VARCHAR(255),
    improvement VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE brain_reasoning_traces (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    step_number INT,
    step_description VARCHAR(255),
    thought_process VARCHAR(1000),
    action_taken VARCHAR(255),
    timestamp TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_brain_reasoning_traces_decision_id ON brain_reasoning_traces (decision_id);

-- 2. Agents-runtime module
CREATE TABLE agent_runtimes (
    id UUID PRIMARY KEY,
    runtime_id UUID NOT NULL,
    agent_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_task VARCHAR(255),
    started_at TIMESTAMP,
    stopped_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE agent_runtime_tasks (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    agent_id UUID NOT NULL,
    goal VARCHAR(1000) NOT NULL,
    input_context VARCHAR(1000),
    result VARCHAR(1000),
    status VARCHAR(50) NOT NULL,
    execution_time BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_agent_runtime_tasks_agent_id ON agent_runtime_tasks (agent_id);

CREATE TABLE agent_runtime_messages (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    sender VARCHAR(255) NOT NULL,
    receiver VARCHAR(255) NOT NULL,
    message_content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

-- 3. Reporting module
CREATE TABLE reports (
    id UUID PRIMARY KEY,
    report_id UUID NOT NULL,
    execution_id UUID NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    report_version VARCHAR(50),
    quality_score DOUBLE PRECISION,
    automation_score DOUBLE PRECISION,
    risk_score DOUBLE PRECISION,
    generated_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_reports_execution_id ON reports (execution_id);

CREATE TABLE report_artifacts (
    id UUID PRIMARY KEY,
    report_id UUID NOT NULL,
    html_path VARCHAR(2000),
    pdf_path VARCHAR(2000),
    json_path VARCHAR(2000),
    junit_xml_path VARCHAR(2000),
    archive_path VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_report_artifacts_report_id ON report_artifacts (report_id);

CREATE TABLE reporting_trends (
    id UUID PRIMARY KEY,
    trend_date DATE NOT NULL,
    pass_rate DOUBLE PRECISION,
    failure_rate DOUBLE PRECISION,
    flaky_rate DOUBLE PRECISION,
    average_duration BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE failure_analyses (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL,
    test_case VARCHAR(255) NOT NULL,
    root_cause VARCHAR(2000),
    recommendation VARCHAR(2000),
    confidence DOUBLE PRECISION,
    ai_generated BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_failure_analyses_execution_id ON failure_analyses (execution_id);
