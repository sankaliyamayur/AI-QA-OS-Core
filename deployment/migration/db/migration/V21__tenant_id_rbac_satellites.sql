-- FI-ENT1-D slice 2 (ADR-055 / ADR-057): tenant-scope RBAC user satellites & audit attribution.
--
-- Adds tenant_id to user satellite tables (security_user_sessions, security_api_keys,
-- security_password_history) for Hibernate @TenantId row-level isolation, and to
-- security_audit_logs for cross-tenant operator attribution.
--
-- Existing rows backfill to the system tenant ('__system__'); ddl-auto: validate stays green.
-- Owner: ai-qa-os-gateway (single Flyway owner, ADR-024).

ALTER TABLE security_user_sessions    ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE security_api_keys         ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE security_password_history ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';
ALTER TABLE security_audit_logs       ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '__system__';

CREATE INDEX ix_user_sessions_tenant ON security_user_sessions    (tenant_id);
CREATE INDEX ix_api_keys_tenant      ON security_api_keys         (tenant_id);
CREATE INDEX ix_password_hist_tenant ON security_password_history (tenant_id);
CREATE INDEX ix_security_audit_tenant ON security_audit_logs       (tenant_id);
