-- FI-ENT1-D (ADR-055): tenant-scoped RBAC — reconcile the user tenant discriminator + per-tenant identity.
--
-- 1) Convert security_users.tenant_id from UUID to the String discriminator used everywhere else
--    (TenantContext / @TenantId, FI-ENT1-C). Existing UUID values become their text form; NULLs (e.g.
--    the bootstrap admin) become the system tenant so nothing is orphaned.
-- 2) Swap the GLOBAL unique constraints on username/email for PER-TENANT ones — the same username may
--    now exist in different tenants (acme/alice ≠ beta/alice).
--
-- Owner: ai-qa-os-gateway (single Flyway owner, ADR-024). Postgres syntax (tests use Hibernate DDL).

ALTER TABLE security_users
    ALTER COLUMN tenant_id TYPE VARCHAR(64) USING COALESCE(tenant_id::text, '__system__');
UPDATE security_users SET tenant_id = '__system__' WHERE tenant_id IS NULL;
ALTER TABLE security_users ALTER COLUMN tenant_id SET DEFAULT '__system__';
ALTER TABLE security_users ALTER COLUMN tenant_id SET NOT NULL;

-- Drop the old global-unique constraints (Postgres auto-named them on the inline UNIQUE columns).
ALTER TABLE security_users DROP CONSTRAINT IF EXISTS security_users_username_key;
ALTER TABLE security_users DROP CONSTRAINT IF EXISTS security_users_email_key;

-- Per-tenant identity uniqueness + a lookup index on the discriminator.
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_tenant_username ON security_users (tenant_id, username);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_tenant_email    ON security_users (tenant_id, email);
CREATE INDEX        IF NOT EXISTS ix_users_tenant          ON security_users (tenant_id);
