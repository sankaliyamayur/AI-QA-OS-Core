package com.aiqaos.testdata.security;

/**
 * MOD-4: how a PII value is masked.
 *
 * <ul>
 *   <li>{@link #REDACT} — replace entirely with a fixed token ({@code [REDACTED]}).</li>
 *   <li>{@link #PARTIAL} — format-preserving partial mask (e.g. {@code j***@e***.com},
 *       {@code ************4444}); the default.</li>
 *   <li>{@link #HASH} — deterministic SHA-256 token; the same input always yields the same token,
 *       so referential integrity (keys / joins) survives across a dataset (pseudonymisation).</li>
 *   <li>{@link #FAKE} — replace with a synthetic but realistic-looking value.</li>
 * </ul>
 */
public enum MaskingStrategy {
    REDACT,
    PARTIAL,
    HASH,
    FAKE
}
