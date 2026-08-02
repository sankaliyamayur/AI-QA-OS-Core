package com.aiqaos.integration.plugin.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.integration.plugin.Plugin;
import com.aiqaos.integration.plugin.PluginManifest;
import com.aiqaos.integration.plugin.PluginProperties;
import com.aiqaos.integration.plugin.PluginRegistry;
import com.aiqaos.integration.plugin.marketplace.PluginCatalog;
import com.aiqaos.integration.plugin.marketplace.PluginListing;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * FI-DX5-A: an end-to-end walk through the plugin toolchain this session built, and a reference for
 * plugin authors — declare a plugin in {@code plugin.json}, load it (DX-5), publish it to the
 * marketplace catalog (PLG-4), then register + run it under the governed lifecycle (PLG-1).
 */
class PluginSdkEndToEndTest {

    /** Reference example plugin: implements the SPI and flips a flag on enable/disable. */
    static final class ExamplePlugin implements Plugin {
        boolean enabled = false;

        @Override public String id() { return "com.acme.sample"; }
        @Override public void onEnable() { enabled = true; }
        @Override public void onDisable() { enabled = false; }
    }

    @Test
    void manifestJson_toCatalog_toGovernedLifecycle() {
        // 1. DX-5 — declare the plugin in plugin.json; load + validate it into a manifest.
        PluginManifest manifest = new PluginManifestLoader(new ObjectMapper())
                .loadFromClasspath("test-plugin.json");
        assertEquals("com.acme.sample", manifest.getId());

        // 2. PLG-4 — publish it to the marketplace catalog and discover it.
        PluginCatalog catalog = new PluginCatalog(new ExtensionSdkProperties());
        catalog.publish(PluginListing.from(manifest, "Sample Plugin", "reference example", "reporting", "acme"));
        assertTrue(catalog.find("com.acme.sample").isPresent(), "listing discoverable in the catalog");

        // 3. PLG-1 — register under governance (SDK compat + granted permissions) and drive the lifecycle.
        PluginProperties props = new PluginProperties();
        props.setGrantedPermissions(Set.of("network.http"));   // test-plugin.json requires network.http
        PluginRegistry registry = new PluginRegistry(props);

        ExamplePlugin plugin = new ExamplePlugin();
        registry.register(plugin, manifest);
        registry.initialize(plugin.id());
        registry.enable(plugin.id());

        assertTrue(plugin.enabled, "onEnable invoked through the registry lifecycle");
        assertEquals(1, registry.enabled().size());
        assertEquals("com.acme.sample", registry.enabled().get(0).getId());
    }
}
