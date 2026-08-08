-- FI-PE3-C (PE-3): make per-execution prompt history recordable and identifiable.
--
-- Context: prompt_executions was created in V5 but has NEVER had a producer -- PromptExecutionRepository
-- had zero callers anywhere in the codebase, so the table is empty in every environment and each change
-- below needs no data migration (the same reasoning V6 used for its drop+recreate).
--
-- Owner: ai-qa-os-gateway (single Flyway owner, ADR-024).

-- 1) final_compiled_prompt was declared OID to match Hibernate's default @Lob String mapping (V5/V6).
--    A Postgres large object is NOT removed when its owning row is deleted, so a per-render producer
--    would leak one orphaned large object per prompt, forever, with no lo_unlink anywhere in the code.
--    TEXT is TOASTed automatically, needs no out-of-band cleanup, and is what every other long-text
--    column in this schema uses (ModuleEntity/TestCaseEntity use @Column(columnDefinition = "TEXT")).
ALTER TABLE prompt_executions DROP COLUMN final_compiled_prompt;
ALTER TABLE prompt_executions ADD COLUMN final_compiled_prompt TEXT NOT NULL DEFAULT '';

-- 2) The row identified its prompt only by template_id/version_id UUIDs, which are populated only when
--    a template is registered in prompt_templates. Prompts are loaded from the classpath by PromptLoader
--    (PromptVersionManager falls back to the "latest" label when the DB row is absent), so in practice
--    both UUIDs are null and the history would have been anonymous. Record the resolved name + label.
ALTER TABLE prompt_executions ADD COLUMN template_name VARCHAR(200);
ALTER TABLE prompt_executions ADD COLUMN version_label VARCHAR(100);

-- 3) No run key -- a prompt execution could not be tied back to the workflow run that produced it
--    (already noted as a scope gap in AiAuditService). MNT-6 stamps the run's correlationId into the
--    MDC for the whole pipeline, and agents render in-JVM inside that scope, so it is faithfully
--    available at render time -- the same value every log line for the run carries.
ALTER TABLE prompt_executions ADD COLUMN correlation_id VARCHAR(100);

CREATE INDEX ix_prompt_executions_correlation   ON prompt_executions (correlation_id);
CREATE INDEX ix_prompt_executions_template_name ON prompt_executions (template_name);
CREATE INDEX ix_prompt_executions_created_at    ON prompt_executions (created_at);
