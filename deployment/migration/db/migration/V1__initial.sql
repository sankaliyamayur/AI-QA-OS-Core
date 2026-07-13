-- Schema creation for core components
CREATE TABLE workflow_definitions (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    definition_json TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE execution_records (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    result_summary TEXT,
    created_at TIMESTAMP NOT NULL
);