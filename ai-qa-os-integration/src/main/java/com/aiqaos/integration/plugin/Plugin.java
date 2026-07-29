package com.aiqaos.integration.plugin;

/**
 * PLG-1: the first-class plugin SPI. An extension implements this and declares a
 * {@link PluginManifest}; the {@link PluginRegistry} drives it through a managed lifecycle. The
 * lifecycle hooks default to no-ops so a plugin overrides only what it needs.
 */
public interface Plugin {

    /** Stable unique plugin id (must match the manifest id). */
    String id();

    /** Called once after registration, before the plugin is enabled. */
    default void initialize(PluginContext context) {
        // no-op by default
    }

    /** Called when the plugin is enabled (may be called again after a disable). */
    default void onEnable() {
        // no-op by default
    }

    /** Called when the plugin is disabled. */
    default void onDisable() {
        // no-op by default
    }
}
