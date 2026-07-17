-- Hibernate's default @ElementCollection table naming uses the entity's JPA entity name
-- (its simple class name, "ApiKeyEntity" -> api_key_entity), not the @Table(name=...) value
-- ("security_api_keys") - so V5's security_api_keys_permissions guess was wrong. It's left
-- in place as an unused table (harmless, empty) and the correctly-named table added here.

CREATE TABLE api_key_entity_permissions (
    api_key_entity_id UUID NOT NULL,
    permission VARCHAR(255)
);
CREATE INDEX idx_api_key_entity_permissions_key_id ON api_key_entity_permissions (api_key_entity_id);
