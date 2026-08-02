package com.aiqaos.integration.plugin.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.integration.plugin.PluginManifest;
import com.aiqaos.integration.plugin.SemanticVersion;
import com.aiqaos.integration.plugin.sdk.ExtensionSdkProperties;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** PLG-4 (ADR-080): the marketplace discovery core — governed publish + discovery. */
class PluginCatalogTest {

    private PluginCatalog catalog() {
        ExtensionSdkProperties props = new ExtensionSdkProperties();   // apiVersion default 1.0.0
        return new PluginCatalog(props);
    }

    private static PluginListing listing(String id, String name, String version, String category, Set<String> caps) {
        return new PluginListing(id, name, SemanticVersion.parse(version), SemanticVersion.parse("1.0.0"),
                "desc for " + id, category, "acme", caps);
    }

    @Test
    void publishThenFindAndDiscover() {
        PluginCatalog catalog = catalog();
        catalog.publish(listing("com.acme.exporter", "Report Exporter", "1.0.0", "reporting", Set.of("report.export")));
        catalog.publish(listing("com.acme.jira", "Jira Sync", "2.0.0", "integration", Set.of("alm.sync")));

        assertTrue(catalog.find("com.acme.exporter").isPresent());
        assertEquals(2, catalog.all().size());
        assertEquals(1, catalog.byCategory("reporting").size());
        assertEquals(1, catalog.byCapability("report.export").size());
        assertEquals(1, catalog.search("jira").size(), "matches by name");
        assertEquals(2, catalog.search("  ").size(), "blank query returns all");
    }

    @Test
    void newerVersionSupersedesAsLatest_butVersionsKeepsBoth() {
        PluginCatalog catalog = catalog();
        catalog.publish(listing("p", "P", "1.0.0", "misc", Set.of()));
        catalog.publish(listing("p", "P", "1.2.0", "misc", Set.of()));

        assertEquals(SemanticVersion.parse("1.2.0"), catalog.find("p").orElseThrow().version(), "find = latest");
        assertEquals(2, catalog.versions("p").size());
        assertEquals(SemanticVersion.parse("1.0.0"), catalog.versions("p").get(0).version(), "versions sorted oldest-first");
    }

    @Test
    void duplicateIdAtVersion_isRejected() {
        PluginCatalog catalog = catalog();
        catalog.publish(listing("p", "P", "1.0.0", "misc", Set.of()));
        assertThrows(PluginCatalogException.class,
                () -> catalog.publish(listing("p", "P", "1.0.0", "misc", Set.of())));
    }

    @Test
    void blankIdOrName_isRejected() {
        PluginCatalog catalog = catalog();
        assertThrows(PluginCatalogException.class,
                () -> catalog.publish(listing("  ", "P", "1.0.0", "misc", Set.of())));
        assertThrows(PluginCatalogException.class,
                () -> catalog.publish(listing("p", "  ", "1.0.0", "misc", Set.of())));
    }

    @Test
    void sdkIncompatibleListing_isRejected() {
        PluginCatalog catalog = catalog();   // runtime SDK 1.0.0
        PluginListing incompatible = new PluginListing("p", "P", SemanticVersion.parse("1.0.0"),
                SemanticVersion.parse("2.0.0"), "d", "misc", "acme", Set.of());   // targets SDK 2.0.0
        assertThrows(PluginCatalogException.class, () -> catalog.publish(incompatible));
    }

    @Test
    void from_mapsManifestPlusMetadata() {
        PluginManifest manifest = new PluginManifest("com.acme.p", SemanticVersion.parse("3.1.0"),
                SemanticVersion.parse("1.0.0"), Set.of("test.generate"), Set.of("network.http"));
        PluginListing l = PluginListing.from(manifest, "My Plugin", "does things", "testing", "acme");

        assertEquals("com.acme.p", l.id());
        assertEquals("My Plugin", l.name());
        assertEquals(SemanticVersion.parse("3.1.0"), l.version());
        assertTrue(l.capabilities().contains("test.generate"));
    }

    @Test
    void emptyCatalog_findAndListsAreEmpty() {
        PluginCatalog catalog = catalog();
        assertTrue(catalog.find("nope").isEmpty());
        assertEquals(List.of(), catalog.versions("nope"));
        assertTrue(catalog.all().isEmpty());
    }
}
