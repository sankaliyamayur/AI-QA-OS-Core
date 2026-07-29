package com.aiqaos.intelligence.version;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * GOV-4: turns version <em>history</em> into version <em>control</em>. A {@link VersionRegistry}
 * governs which version of a registry key (e.g. {@code prompt:greeting}) is authoritative:
 *
 * <ul>
 *   <li>{@link #pin} promotes an approved version to active (deactivating the prior one);</li>
 *   <li>{@link #rollback} instantly reverts to the immediately-previous distinct version
 *       (last-known-good) as a fresh, audited pin;</li>
 *   <li>{@link #activeVersion} answers "what runs now";</li>
 *   <li>{@link #history} is the pin/rollback timeline.</li>
 * </ul>
 *
 * <p>The registry is generic over {@link VersionKind}, so it already models both prompt and model
 * versions; it is grounded on prompt versions today (model-version governance is FI-GOV4-A). All
 * logic sits over the {@link VersionPinStore} seam and is fully unit-testable without a database.
 */
@Service
public class VersionRegistry {

    private final VersionPinStore store;
    private final Supplier<LocalDateTime> clock;

    @Autowired
    public VersionRegistry(VersionPinStore store) {
        this(store, LocalDateTime::now);
    }

    /** Test seam: supply a clock to make {@code pinnedAt} deterministic. */
    VersionRegistry(VersionPinStore store, Supplier<LocalDateTime> clock) {
        this.store = store;
        this.clock = clock;
    }

    /**
     * Promote {@code versionTag} to the active version of {@code registryKey}. Idempotent: pinning
     * the already-active version returns the existing pin without appending a duplicate.
     */
    public VersionPin pin(String registryKey, VersionKind kind, String versionTag, String actor) {
        Optional<VersionPin> current = store.activePin(registryKey);
        if (current.isPresent() && current.get().getVersionTag().equals(versionTag)) {
            return current.get(); // already active — no-op
        }
        VersionPin pin = new VersionPin(registryKey, kind, versionTag, actor, clock.get(), true);
        store.save(pin); // store deactivates the prior active pin
        return pin;
    }

    /**
     * Instantly revert {@code registryKey} to its immediately-previous distinct version (the
     * last-known-good), recorded as a new active pin so the rollback itself is auditable. Returns
     * empty (a safe no-op) if the key was never pinned or has no earlier distinct version.
     */
    public Optional<VersionPin> rollback(String registryKey, String actor) {
        List<VersionPin> history = store.history(registryKey); // newest first
        if (history.isEmpty()) {
            return Optional.empty();
        }
        String currentTag = history.get(0).getVersionTag();
        for (VersionPin past : history) {
            if (!past.getVersionTag().equals(currentTag)) {
                // Re-pin the previous distinct version, preserving its kind.
                return Optional.of(pin(registryKey, past.getKind(), past.getVersionTag(), actor));
            }
        }
        return Optional.empty(); // only ever one distinct version — nothing to roll back to
    }

    /** The version tag currently pinned for {@code registryKey} (the authority on what runs). */
    public Optional<String> activeVersion(String registryKey) {
        return store.activePin(registryKey).map(VersionPin::getVersionTag);
    }

    /** The pin/rollback timeline for {@code registryKey}, newest first. */
    public List<VersionPin> history(String registryKey) {
        return store.history(registryKey);
    }
}
