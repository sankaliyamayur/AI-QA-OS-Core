package com.aiqaos.intelligence.version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * GOV-4: in-memory reference implementation of {@link VersionPinStore} — the default store and the
 * one the registry logic is unit-tested against. Insertion order <em>is</em> the timeline (no clock
 * dependence), so history/rollback are deterministic. For durability across restarts, set
 * {@code aiqaos.version-registry.store=jpa} to activate {@code JpaVersionPinStore} instead.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.version-registry.store", havingValue = "memory",
        matchIfMissing = true)
public class InMemoryVersionPinStore implements VersionPinStore {

    /** Append-only pin log; newest at the tail. Thread-safe for concurrent pin/read. */
    private final List<VersionPin> pins = new CopyOnWriteArrayList<>();

    @Override
    public synchronized void save(VersionPin pin) {
        if (pin.isActive()) {
            // Enforce the single-active invariant: deactivate the current active pin for this key.
            for (int i = 0; i < pins.size(); i++) {
                VersionPin existing = pins.get(i);
                if (existing.isActive() && existing.getRegistryKey().equals(pin.getRegistryKey())) {
                    pins.set(i, existing.deactivated());
                }
            }
        }
        pins.add(pin);
    }

    @Override
    public Optional<VersionPin> activePin(String registryKey) {
        VersionPin found = null;
        for (VersionPin p : pins) {
            if (p.isActive() && p.getRegistryKey().equals(registryKey)) {
                found = p; // last active wins (there is at most one, but be defensive)
            }
        }
        return Optional.ofNullable(found);
    }

    @Override
    public List<VersionPin> history(String registryKey) {
        List<VersionPin> out = new ArrayList<>();
        for (VersionPin p : pins) {
            if (p.getRegistryKey().equals(registryKey)) {
                out.add(p);
            }
        }
        Collections.reverse(out); // newest first
        return out;
    }
}
