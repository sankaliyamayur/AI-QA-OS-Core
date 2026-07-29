package com.aiqaos.integration.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PLG-1: the in-process plugin runtime. Registers plugins under governance (unique id, SDK-version
 * compatibility, granted permissions) and drives them through the managed lifecycle
 * ({@code register → initialize → enable ⇄ disable}), invoking the {@link Plugin} hooks and refusing
 * invalid transitions. Dynamic loading / signing / sandboxing are deferred (FI-PLG1-A/B).
 */
@Component
public class PluginRegistry {

    private static final Logger log = LoggerFactory.getLogger(PluginRegistry.class);

    private final PluginProperties properties;
    private final Map<String, PluginDescriptor> byId = new ConcurrentHashMap<>();

    public PluginRegistry(PluginProperties properties) {
        this.properties = properties;
    }

    /**
     * Register a plugin. Rejects (with {@link PluginRegistrationException}) a duplicate id, an
     * id/manifest mismatch, an incompatible SDK version, or a required permission not granted.
     */
    public synchronized PluginDescriptor register(Plugin plugin, PluginManifest manifest) {
        if (plugin == null || manifest == null) {
            throw new PluginRegistrationException("plugin and manifest are required");
        }
        if (!plugin.id().equals(manifest.getId())) {
            throw new PluginRegistrationException(
                    "plugin id '" + plugin.id() + "' does not match manifest id '" + manifest.getId() + "'");
        }
        if (byId.containsKey(manifest.getId())) {
            throw new PluginRegistrationException("duplicate plugin id: " + manifest.getId());
        }

        SemanticVersion runtime = SemanticVersion.parse(properties.getSdkApiVersion());
        if (manifest.getSdkApiVersion() == null || !manifest.getSdkApiVersion().isCompatibleWith(runtime)) {
            throw new PluginRegistrationException("plugin '" + manifest.getId() + "' targets SDK API "
                    + manifest.getSdkApiVersion() + " incompatible with runtime " + runtime);
        }

        for (String perm : manifest.getRequiredPermissions()) {
            if (!properties.getGrantedPermissions().contains(perm)) {
                throw new PluginRegistrationException("plugin '" + manifest.getId()
                        + "' requires ungranted permission: " + perm);
            }
        }

        PluginDescriptor descriptor = new PluginDescriptor(plugin, manifest, PluginState.REGISTERED);
        byId.put(manifest.getId(), descriptor);
        log.info("[PluginRegistry] registered {} v{}", manifest.getId(), manifest.getVersion());
        return descriptor;
    }

    /** {@code REGISTERED → INITIALIZED}, invoking {@link Plugin#initialize}. */
    public void initialize(String id) {
        PluginDescriptor d = require(id);
        expect(d, PluginState.REGISTERED, "initialize");
        runHook(d, () -> d.getPlugin().initialize(
                new PluginContext(properties.getGrantedPermissions(), Map.of())), PluginState.INITIALIZED);
    }

    /** {@code INITIALIZED} or {@code DISABLED} → {@code ENABLED}, invoking {@link Plugin#onEnable}. */
    public void enable(String id) {
        PluginDescriptor d = require(id);
        if (d.getState() != PluginState.INITIALIZED && d.getState() != PluginState.DISABLED) {
            throw new IllegalStateException("cannot enable " + id + " from state " + d.getState());
        }
        runHook(d, () -> d.getPlugin().onEnable(), PluginState.ENABLED);
    }

    /** {@code ENABLED → DISABLED}, invoking {@link Plugin#onDisable}. */
    public void disable(String id) {
        PluginDescriptor d = require(id);
        expect(d, PluginState.ENABLED, "disable");
        runHook(d, () -> d.getPlugin().onDisable(), PluginState.DISABLED);
    }

    public Optional<PluginDescriptor> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<PluginDescriptor> all() {
        return new ArrayList<>(byId.values());
    }

    public List<PluginDescriptor> enabled() {
        List<PluginDescriptor> out = new ArrayList<>();
        for (PluginDescriptor d : byId.values()) {
            if (d.getState() == PluginState.ENABLED) {
                out.add(d);
            }
        }
        return out;
    }

    private PluginDescriptor require(String id) {
        PluginDescriptor d = byId.get(id);
        if (d == null) {
            throw new IllegalArgumentException("unknown plugin: " + id);
        }
        return d;
    }

    private void expect(PluginDescriptor d, PluginState expected, String action) {
        if (d.getState() != expected) {
            throw new IllegalStateException("cannot " + action + " " + d.getId()
                    + " from state " + d.getState() + " (expected " + expected + ")");
        }
    }

    private void runHook(PluginDescriptor d, Runnable hook, PluginState onSuccess) {
        try {
            hook.run();
            d.setState(onSuccess);
        } catch (RuntimeException ex) {
            d.setState(PluginState.FAILED);
            log.error("[PluginRegistry] {} failed transitioning to {}: {}", d.getId(), onSuccess, ex.getMessage());
            throw ex;
        }
    }
}
