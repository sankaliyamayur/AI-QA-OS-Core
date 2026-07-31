-- FI-ENT1-E (ADR-056): tenant-scope the durable memory + cost surfaces via the @TenantId discriminator.
--
-- Adds tenant_id to the memory (nodes, conversation history) and LLM-cost tables so Hibernate stamps
-- it on insert and filters it on read — a tenant sees only its own memory + cost rows. Existing rows
-- are backfilled to the system tenant ('__system__') so pre-tenancy data stays visible to system work.
--
-- Owner: ai-qa-os-gateway (single Flyway owner, ADR-024). Type matches TenantContext (String).

ALTER TABLE memory_nodes            ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE conversation_histories  ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE observability_llm_costs ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';

CREATE INDEX ix_memory_nodes_tenant      ON memory_nodes            (tenant_id);
CREATE INDEX ix_conversation_hist_tenant ON conversation_histories  (tenant_id);
CREATE INDEX ix_llm_costs_tenant         ON observability_llm_costs (tenant_id);
