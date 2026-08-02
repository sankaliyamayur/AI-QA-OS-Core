package com.aiqaos.integration.plugin.marketplace;

import com.aiqaos.integration.plugin.PluginManifest;
import com.aiqaos.integration.plugin.SemanticVersion;
import java.util.Set;

/**
 * PLG-4 (ADR-080): a marketplace catalog entry — an <b>available/publishable</b> plugin (as opposed to
 * PLG-1's {@code PluginRegistry}, which holds <b>installed/running</b> plugins). Combines the manifest
 * facts (id / version / sdkApiVersion / capabilities) with marketplace metadata (name / description /
 * category / author). {@link #from} builds a listing straight from a DX-5 {@link PluginManifest}.
 *
 * @param id             stable plugin id
 * @param name           human-readable marketplace name
 * @param version        the plugin's version
 * @param sdkApiVersion  the SDK API version it targets (governed at publish)
 * @param description    marketplace description (may be blank)
 * @param category       marketplace category (free-form; categories evolve)
 * @param author         publisher/author (may be blank)
 * @param capabilities   declared capabilities
 */
public record PluginListing(
        String id,
        String name,
        SemanticVersion version,
        SemanticVersion sdkApiVersion,
        String description,
        String category,
        String author,
        Set<String> capabilities) {

    public PluginListing {
        capabilities = capabilities != null ? Set.copyOf(capabilities) : Set.of();
    }

    /** Build a listing from a published manifest (DX-5) plus marketplace metadata. */
    public static PluginListing from(PluginManifest manifest, String name, String description,
                                     String category, String author) {
        return new PluginListing(
                manifest.getId(), name, manifest.getVersion(), manifest.getSdkApiVersion(),
                description, category, author, manifest.getCapabilities());
    }
}
