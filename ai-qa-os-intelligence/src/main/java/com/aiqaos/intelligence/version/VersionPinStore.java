package com.aiqaos.intelligence.version;

import java.util.List;
import java.util.Optional;

/**
 * GOV-4 seam: durable storage for version pins, isolated so the pin/rollback/history <em>logic</em>
 * ({@link VersionRegistry}) is fully unit-testable against an in-memory reference
 * ({@link InMemoryVersionPinStore}) while production runs over JPA ({@code JpaVersionPinStore}).
 * Same pattern as ENT-5's {@code ObjectStorageClient}.
 *
 * <p><b>Invariant the store enforces:</b> saving an <i>active</i> pin deactivates any previously
 * active pin for the same {@code registryKey}, so at most one pin per key is active.
 */
public interface VersionPinStore {

    /**
     * Persist a pin. If the pin is active, atomically deactivate the current active pin for the same
     * {@code registryKey} first (single-active invariant).
     */
    void save(VersionPin pin);

    /** The currently active pin for {@code registryKey}, or empty if the key was never pinned. */
    Optional<VersionPin> activePin(String registryKey);

    /** Full pin timeline for {@code registryKey}, newest first (the governance history). */
    List<VersionPin> history(String registryKey);
}
