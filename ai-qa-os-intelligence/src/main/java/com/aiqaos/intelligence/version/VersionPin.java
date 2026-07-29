package com.aiqaos.intelligence.version;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * GOV-4: an immutable governance record that a specific {@code versionTag} was made the active
 * ("pinned") version of a registry key by an {@code actor} at {@code pinnedAt}. A pin is the
 * authority on "what runs"; a rollback is simply a new pin of an earlier version. Exactly one pin
 * per {@code registryKey} is active at a time (enforced by the store).
 *
 * <p>This is the domain value object; durable persistence lives in {@code VersionPinEntity} behind
 * the {@link VersionPinStore} seam.
 */
public final class VersionPin {

    private final String registryKey;
    private final VersionKind kind;
    private final String versionTag;
    private final String actor;
    private final LocalDateTime pinnedAt;
    private final boolean active;

    public VersionPin(String registryKey, VersionKind kind, String versionTag, String actor,
                      LocalDateTime pinnedAt, boolean active) {
        this.registryKey = Objects.requireNonNull(registryKey, "registryKey");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.versionTag = Objects.requireNonNull(versionTag, "versionTag");
        this.actor = actor;
        this.pinnedAt = pinnedAt;
        this.active = active;
    }

    /** Returns a copy of this pin with its active flag flipped to {@code false} (deactivation). */
    public VersionPin deactivated() {
        return active ? new VersionPin(registryKey, kind, versionTag, actor, pinnedAt, false) : this;
    }

    public String getRegistryKey() { return registryKey; }
    public VersionKind getKind() { return kind; }
    public String getVersionTag() { return versionTag; }
    public String getActor() { return actor; }
    public LocalDateTime getPinnedAt() { return pinnedAt; }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return "VersionPin{" + registryKey + " -> " + versionTag + " (" + kind + ")"
                + (active ? " ACTIVE" : "") + " by " + actor + "}";
    }
}
