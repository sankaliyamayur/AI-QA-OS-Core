package com.aiqaos.integration.plugin;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * PLG-1: what a {@link Plugin} receives at {@code initialize} — the permissions the runtime granted
 * it and its configuration. A plugin must operate only within its granted permissions (the registry
 * refuses to register a plugin whose required permissions aren't granted).
 */
public final class PluginContext {

    private final Set<String> grantedPermissions;
    private final Map<String, Object> config;

    public PluginContext(Set<String> grantedPermissions, Map<String, Object> config) {
        this.grantedPermissions = Collections.unmodifiableSet(
                new LinkedHashSet<>(grantedPermissions != null ? grantedPermissions : Set.of()));
        this.config = config != null ? Map.copyOf(config) : Map.of();
    }

    public Set<String> getGrantedPermissions() { return grantedPermissions; }
    public Map<String, Object> getConfig() { return config; }
}
