-- GOV-4: durable version pins. One row per pin/rollback event; at most one active pin per
-- registry_key. The registry is generic (kind PROMPT/MODEL) and grounded on prompt versions today.
CREATE TABLE version_pins (
    id               UUID PRIMARY KEY,
    registry_key     VARCHAR(255) NOT NULL,
    kind             VARCHAR(32)  NOT NULL,
    version_tag      VARCHAR(255) NOT NULL,
    actor            VARCHAR(255),
    pinned_at        TIMESTAMP,
    active_pin       BOOLEAN NOT NULL DEFAULT FALSE,

    -- BaseEntity audit columns
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    version          BIGINT,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_version_pins_key ON version_pins (registry_key);
-- Fast active-pin lookup ("what runs now") per key.
CREATE INDEX idx_version_pins_active ON version_pins (registry_key, active_pin);
