package com.aiqaos.execution.artifact;

import java.time.Instant;
import java.util.List;

/**
 * SCALE-1: the seam for storing/retrieving execution artifacts by <b>key</b> rather than host path,
 * so a cross-host worker's artifacts are addressable from anywhere. {@link LocalArtifactStore} is
 * the single-host reference; ENT-5 adds {@link ObjectStorageArtifactStore} (object storage) behind
 * this same seam — the code-level unblock for horizontal execution.
 *
 * <p>ENT-5 also adds {@link #list}/{@link #delete}/{@link #lastModified} so the artifact tree can be
 * enumerated and age-purged ({@link com.aiqaos.execution.lifecycle.ArtifactRetentionService}).
 */
public interface ArtifactStore {

    /** Store {@code content} under {@code key}; returns the key by which it can be resolved. */
    String store(String key, byte[] content);

    /** Read the content previously stored under {@code key}. */
    byte[] resolve(String key);

    /** Whether an artifact exists under {@code key}. */
    boolean exists(String key);

    /** All artifact keys under {@code prefix} (empty/null prefix = all). */
    List<String> list(String prefix);

    /** Delete the artifact under {@code key} (no-op if absent). */
    void delete(String key);

    /** When the artifact under {@code key} was last modified — used for age-based retention. */
    Instant lastModified(String key);
}
