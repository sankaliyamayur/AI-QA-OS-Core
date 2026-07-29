package com.aiqaos.integration.plugin.integration;

import com.aiqaos.integration.plugin.PluginRegistrationException;
import com.aiqaos.integration.plugin.PluginRegistry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PLG-2: admits every {@link IntegrationPlugin} bean into the PLG-1 {@link PluginRegistry} under
 * governance (register → initialize → enable). A plugin whose required permission isn't granted (or
 * whose SDK version is incompatible) is <b>skipped</b> and logged, not fatal — so integrations are
 * off-by-default until an operator grants {@code aiqaos.plugins.granted-permissions}.
 */
@Component
public class IntegrationPluginRegistrar {

    private static final Logger log = LoggerFactory.getLogger(IntegrationPluginRegistrar.class);

    private final PluginRegistry registry;
    private final List<IntegrationPlugin> plugins;

    public IntegrationPluginRegistrar(PluginRegistry registry, List<IntegrationPlugin> plugins) {
        this.registry = registry;
        this.plugins = plugins;
    }

    /** Admit all integration plugins; returns how many were registered + enabled. */
    public int registerAll() {
        int admitted = 0;
        for (IntegrationPlugin plugin : plugins) {
            try {
                registry.register(plugin, plugin.manifest());
                registry.initialize(plugin.id());
                registry.enable(plugin.id());
                admitted++;
                log.info("[IntegrationPlugins] admitted {} ({})", plugin.id(), plugin.category());
            } catch (PluginRegistrationException ex) {
                log.warn("[IntegrationPlugins] skipped {} — {}", plugin.id(), ex.getMessage());
            }
        }
        return admitted;
    }
}
