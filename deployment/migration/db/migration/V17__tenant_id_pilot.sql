-- FI-ENT1-C (ADR-054): row-level persistence tenancy for the pilot aggregate.
--
-- Adds the `tenant_id` discriminator column that Hibernate's @TenantId multi-tenancy stamps on
-- INSERT and filters on SELECT for the test-management + execution entities. Existing ("legacy")
-- rows are backfilled to the system tenant ('__system__') so current no-`X-Tenant-ID` flows keep
-- working with no regression; new tenant-bound requests get their real tenant id.
--
-- Owner: ai-qa-os-gateway (single Flyway owner, ADR-024). Type matches the entities' String tenant id
-- (TenantContext.SYSTEM_TENANT = '__system__'). Indexed to support the auto-appended WHERE clause.

ALTER TABLE modules              ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE test_cases           ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE executions           ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE execution_steps      ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE execution_artifacts  ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE workflow_executions  ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE human_reviews        ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';

CREATE INDEX ix_modules_tenant             ON modules             (tenant_id);
CREATE INDEX ix_test_cases_tenant          ON test_cases          (tenant_id);
CREATE INDEX ix_executions_tenant          ON executions          (tenant_id);
CREATE INDEX ix_execution_steps_tenant     ON execution_steps     (tenant_id);
CREATE INDEX ix_execution_artifacts_tenant ON execution_artifacts (tenant_id);
CREATE INDEX ix_workflow_executions_tenant ON workflow_executions (tenant_id);
CREATE INDEX ix_human_reviews_tenant       ON human_reviews       (tenant_id);
