-- UserEntity gained multi-tenancy, MFA, and account-lockout fields after V2 was written;
-- V2 (already applied/checksummed) never got updated to match. Add the missing columns
-- and the backup-codes element-collection table (default Hibernate naming: entity name
-- "UserEntity" -> user_entity_backup_codes, not the @Table-based security_users).

ALTER TABLE security_users ADD COLUMN tenant_id UUID;
ALTER TABLE security_users ADD COLUMN organization_id UUID;
ALTER TABLE security_users ADD COLUMN workspace_id UUID;
ALTER TABLE security_users ADD COLUMN mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE security_users ADD COLUMN mfa_secret VARCHAR(255);
ALTER TABLE security_users ADD COLUMN account_locked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE security_users ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE security_users ADD COLUMN locked_until TIMESTAMP;

CREATE TABLE user_entity_backup_codes (
    user_entity_id UUID NOT NULL,
    backup_code VARCHAR(255)
);
CREATE INDEX idx_user_entity_backup_codes_user_id ON user_entity_backup_codes (user_entity_id);
