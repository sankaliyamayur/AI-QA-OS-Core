package com.aiqaos.intelligence.version;

/**
 * GOV-4: the kind of governed version a pin refers to. The registry is generic over kind so that
 * model-version governance (currently ungoverned strings in {@code ai-provider}) can plug into the
 * same pin/rollback machinery once a {@code ModelVersion} object and a shared placement exist
 * (FI-GOV4-A). Grounded on {@code PROMPT} today.
 */
public enum VersionKind {
    PROMPT,
    MODEL
}
