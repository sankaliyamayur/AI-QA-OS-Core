-- FI-ENT1-C extension (ADR-057): tenant-scope the remaining operational/result tables via @TenantId.
--
-- Conservative boundary: the data a tenant PRODUCES (execution audits, workflows, reports, prompt
-- executions, agent runtime/executions, brain decisions/reasoning/learning, eval results) becomes
-- tenant-isolated. Platform catalogs (prompt templates, agent definitions, roles) and observability
-- telemetry / security audit stay global/system-scoped and are intentionally NOT touched here.
--
-- Existing rows backfill to the system tenant ('__system__'); ddl-auto: validate stays green.
-- Owner: ai-qa-os-gateway (single Flyway owner, ADR-024).

ALTER TABLE execution_audits        ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE workflows               ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE workflow_steps          ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE reports                 ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE report_artifacts        ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE failure_analyses        ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE reporting_trends        ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE prompt_executions       ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE agent_executions        ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE agent_runtime_messages  ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE agent_runtimes          ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE agent_runtime_tasks     ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE brain_decisions         ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE brain_reasoning_traces  ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE brain_learning          ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE eval_results            ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';

CREATE INDEX ix_execution_audits_tenant       ON execution_audits        (tenant_id);
CREATE INDEX ix_workflows_tenant              ON workflows               (tenant_id);
CREATE INDEX ix_workflow_steps_tenant         ON workflow_steps          (tenant_id);
CREATE INDEX ix_reports_tenant                ON reports                 (tenant_id);
CREATE INDEX ix_report_artifacts_tenant       ON report_artifacts        (tenant_id);
CREATE INDEX ix_failure_analyses_tenant       ON failure_analyses        (tenant_id);
CREATE INDEX ix_reporting_trends_tenant       ON reporting_trends        (tenant_id);
CREATE INDEX ix_prompt_executions_tenant      ON prompt_executions       (tenant_id);
CREATE INDEX ix_agent_executions_tenant       ON agent_executions        (tenant_id);
CREATE INDEX ix_agent_runtime_messages_tenant ON agent_runtime_messages  (tenant_id);
CREATE INDEX ix_agent_runtimes_tenant         ON agent_runtimes          (tenant_id);
CREATE INDEX ix_agent_runtime_tasks_tenant    ON agent_runtime_tasks     (tenant_id);
CREATE INDEX ix_brain_decisions_tenant        ON brain_decisions         (tenant_id);
CREATE INDEX ix_brain_reasoning_traces_tenant ON brain_reasoning_traces  (tenant_id);
CREATE INDEX ix_brain_learning_tenant         ON brain_learning          (tenant_id);
CREATE INDEX ix_eval_results_tenant           ON eval_results            (tenant_id);
