package com.aiqaos.integration.plugin;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * PLG-1: a plugin's self-declaration — its id, its own {@code version}, the {@code sdkApiVersion} it
 * targets (checked for compatibility against the runtime), the {@code capabilities} it provides, and
 * the {@code requiredPermissions} it needs (checked against the runtime's granted set at register).
 */
public final class PluginManifest {

    private final String id;
    private final SemanticVersion version;
    private final SemanticVersion sdkApiVersion;
    private final Set<String> capabilities;
    private final Set<String> requiredPermissions;

    public PluginManifest(String id, SemanticVersion version, SemanticVersion sdkApiVersion,
                          Set<String> capabilities, Set<String> requiredPermissions) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("manifest id is blank");
        }
        this.id = id;
        this.version = version;
        this.sdkApiVersion = sdkApiVersion;
        this.capabilities = immutable(capabilities);
        this.requiredPermissions = immutable(requiredPermissions);
    }

    private static Set<String> immutable(Set<String> s) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(s != null ? s : Set.of()));
    }

    public String getId() { return id; }
    public SemanticVersion getVersion() { return version; }
    public SemanticVersion getSdkApiVersion() { return sdkApiVersion; }
    public Set<String> getCapabilities() { return capabilities; }
    public Set<String> getRequiredPermissions() { return requiredPermissions; }
}
