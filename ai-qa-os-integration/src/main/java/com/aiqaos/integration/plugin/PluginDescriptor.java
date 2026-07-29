package com.aiqaos.integration.plugin;

/**
 * PLG-1: the registry's record of a plugin — its {@link Plugin} instance, its {@link PluginManifest},
 * and its current {@link PluginState}.
 */
public final class PluginDescriptor {

    private final Plugin plugin;
    private final PluginManifest manifest;
    private PluginState state;

    public PluginDescriptor(Plugin plugin, PluginManifest manifest, PluginState state) {
        this.plugin = plugin;
        this.manifest = manifest;
        this.state = state;
    }

    public Plugin getPlugin() { return plugin; }
    public PluginManifest getManifest() { return manifest; }
    public PluginState getState() { return state; }
    public void setState(PluginState state) { this.state = state; }

    public String getId() { return manifest.getId(); }

    @Override
    public String toString() {
        return "PluginDescriptor{" + getId() + " v" + manifest.getVersion() + " [" + state + "]}";
    }
}
