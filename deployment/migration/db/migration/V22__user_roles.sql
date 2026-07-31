-- FI-ENT4-C (ADR-066): persist a user's assigned role names, so authentication can derive Spring
-- authorities (ROLE_<name>) and role-based authorization (hasRole('ADMIN')) works. Roles are the
-- global catalog (ADR-055), referenced by name; this is the user↔role mapping.
--
-- Owner: ai-qa-os-gateway (single Flyway owner, ADR-024). Maps UserEntity.roles (@ElementCollection).

CREATE TABLE security_user_roles (
    user_id   UUID         NOT NULL,
    role_name VARCHAR(50)  NOT NULL,
    PRIMARY KEY (user_id, role_name),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES security_users (id) ON DELETE CASCADE
);

CREATE INDEX ix_user_roles_user ON security_user_roles (user_id);
