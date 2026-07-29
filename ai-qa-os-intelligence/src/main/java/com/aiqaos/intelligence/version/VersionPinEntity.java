package com.aiqaos.intelligence.version;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * GOV-4: durable form of a {@link VersionPin}. A governance rollback must survive restarts and be
 * the authority on "what runs", so pins are persisted. Mapped behind {@code JpaVersionPinStore}.
 *
 * <p>Note: the "is this the active pin" flag is {@code activePin}/{@code active_pin} — deliberately
 * distinct from {@link BaseEntity}'s soft-delete {@code active} column, which means something else.
 */
@Entity
@Table(name = "version_pins", indexes = @Index(name = "idx_version_pins_key", columnList = "registry_key"))
public class VersionPinEntity extends BaseEntity {

    @Column(name = "registry_key", nullable = false)
    private String registryKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private VersionKind kind;

    @Column(name = "version_tag", nullable = false)
    private String versionTag;

    @Column(name = "actor")
    private String actor;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    @Column(name = "active_pin", nullable = false)
    private boolean activePin;

    public String getRegistryKey() { return registryKey; }
    public void setRegistryKey(String registryKey) { this.registryKey = registryKey; }

    public VersionKind getKind() { return kind; }
    public void setKind(VersionKind kind) { this.kind = kind; }

    public String getVersionTag() { return versionTag; }
    public void setVersionTag(String versionTag) { this.versionTag = versionTag; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public LocalDateTime getPinnedAt() { return pinnedAt; }
    public void setPinnedAt(LocalDateTime pinnedAt) { this.pinnedAt = pinnedAt; }

    public boolean isActivePin() { return activePin; }
    public void setActivePin(boolean activePin) { this.activePin = activePin; }
}
