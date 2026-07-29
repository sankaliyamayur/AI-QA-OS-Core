package com.aiqaos.integration.plugin.integration;

import com.aiqaos.integration.plugin.PluginManifest;
import com.aiqaos.integration.plugin.SemanticVersion;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * PLG-2: base for integration plugins — holds the {@link PluginManifest} (so {@code id()} and
 * {@code manifest()} come for free) and provides a manifest factory. Concrete plugins define their
 * {@link IntegrationCategory} and {@code execute}.
 */
public abstract class AbstractIntegrationPlugin implements IntegrationPlugin {

    private final PluginManifest manifest;

    protected AbstractIntegrationPlugin(PluginManifest manifest) {
        this.manifest = manifest;
    }

    /** Build a manifest targeting SDK API 1.0.0, requiring the {@code integration.<id>} permission. */
    protected static PluginManifest manifest(String id, Set<String> capabilities) {
        return new PluginManifest(id,
                SemanticVersion.parse("1.0.0"),
                SemanticVersion.parse("1.0.0"),
                capabilities,
                Set.of("integration." + id));
    }

    protected static Set<String> caps(String... values) {
        return new LinkedHashSet<>(Set.of(values));
    }

    @Override
    public String id() {
        return manifest.getId();
    }

    @Override
    public PluginManifest manifest() {
        return manifest;
    }
}
