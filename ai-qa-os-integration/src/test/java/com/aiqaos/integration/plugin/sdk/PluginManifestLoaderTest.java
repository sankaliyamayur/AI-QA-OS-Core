package com.aiqaos.integration.plugin.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.integration.plugin.PluginManifest;
import com.aiqaos.integration.plugin.SemanticVersion;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * DX-5 (ADR-079): the plugin SDK's declarative-manifest loader — parse + validate plugin.json into a
 * PluginManifest, with clear errors. Pure, fully unit-verified.
 */
class PluginManifestLoaderTest {

    private final PluginManifestLoader loader = new PluginManifestLoader(new ObjectMapper());

    @Test
    void loadsFullManifest() {
        PluginManifest m = loader.load("""
            {"id":"com.acme.p","version":"1.2.0","sdkApiVersion":"1.0.0",
             "capabilities":["report.export"],"requiredPermissions":["network.http"]}""");

        assertEquals("com.acme.p", m.getId());
        assertEquals(new SemanticVersion(1, 2, 0), m.getVersion());
        assertEquals(new SemanticVersion(1, 0, 0), m.getSdkApiVersion());
        assertTrue(m.getCapabilities().contains("report.export"));
        assertTrue(m.getRequiredPermissions().contains("network.http"));
    }

    @Test
    void minimalManifest_yieldsEmptySets() {
        PluginManifest m = loader.load("{\"id\":\"p\",\"version\":\"1.0.0\",\"sdkApiVersion\":\"1.0.0\"}");
        assertTrue(m.getCapabilities().isEmpty());
        assertTrue(m.getRequiredPermissions().isEmpty());
    }

    @Test
    void missingId_throws() {
        assertThrows(PluginManifestException.class,
                () -> loader.load("{\"version\":\"1.0.0\",\"sdkApiVersion\":\"1.0.0\"}"));
    }

    @Test
    void unparseableVersion_throws() {
        assertThrows(PluginManifestException.class,
                () -> loader.load("{\"id\":\"p\",\"version\":\"not-a-version\",\"sdkApiVersion\":\"1.0.0\"}"));
    }

    @Test
    void missingSdkApiVersion_throws() {
        assertThrows(PluginManifestException.class,
                () -> loader.load("{\"id\":\"p\",\"version\":\"1.0.0\"}"));
    }

    @Test
    void malformedJson_throws() {
        assertThrows(PluginManifestException.class, () -> loader.load("{not valid json"));
    }

    @Test
    void emptyInput_throws() {
        assertThrows(PluginManifestException.class, () -> loader.load("   "));
    }

    @Test
    void loadFromClasspath_readsBundledManifest() {
        PluginManifest m = loader.loadFromClasspath("test-plugin.json");
        assertEquals("com.acme.sample", m.getId());
        assertEquals(new SemanticVersion(2, 1, 0), m.getVersion());
        assertTrue(m.getCapabilities().contains("test.generate"));
    }

    @Test
    void loadFromClasspath_missingResource_throws() {
        assertThrows(PluginManifestException.class, () -> loader.loadFromClasspath("does-not-exist.json"));
    }
}
