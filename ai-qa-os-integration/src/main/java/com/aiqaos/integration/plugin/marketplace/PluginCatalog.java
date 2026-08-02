package com.aiqaos.integration.plugin.marketplace;

import com.aiqaos.integration.plugin.SemanticVersion;
import com.aiqaos.integration.plugin.sdk.ExtensionSdkProperties;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PLG-4 (ADR-080): the marketplace's discovery core — a governed catalog of <b>available</b> plugin
 * {@link PluginListing}s that can be published and discovered (the seam a marketplace service is built
 * on; the standalone service is v3.0). Distinct from PLG-1's {@code PluginRegistry} (installed/running
 * plugins). Governance mirrors {@code ExtensionRegistry}: a listing's SDK version must be compatible
 * with the runtime, and a given {@code id@version} is published at most once.
 */
@Component
public class PluginCatalog {

    private static final Logger log = LoggerFactory.getLogger(PluginCatalog.class);

    /** id -> (version string -> listing) — multiple versions of a plugin may be catalogued. */
    private final Map<String, Map<String, PluginListing>> byId = new ConcurrentHashMap<>();
    private final ExtensionSdkProperties sdkProperties;

    private static final Comparator<SemanticVersion> BY_VERSION =
            Comparator.comparingInt(SemanticVersion::getMajor)
                    .thenComparingInt(SemanticVersion::getMinor)
                    .thenComparingInt(SemanticVersion::getPatch);

    public PluginCatalog(ExtensionSdkProperties sdkProperties) {
        this.sdkProperties = sdkProperties;
    }

    /**
     * Publish a listing. Rejects (with {@link PluginCatalogException}) a blank id/name, a missing
     * version/sdkApiVersion, an SDK version incompatible with the runtime, or a re-publish of an
     * already-published {@code id@version}. A newer version of an existing id supersedes it as "latest".
     */
    public synchronized PluginListing publish(PluginListing listing) {
        if (listing == null) {
            throw new PluginCatalogException("listing is required");
        }
        if (listing.id() == null || listing.id().isBlank()) {
            throw new PluginCatalogException("listing id is required");
        }
        if (listing.name() == null || listing.name().isBlank()) {
            throw new PluginCatalogException("listing name is required");
        }
        if (listing.version() == null || listing.sdkApiVersion() == null) {
            throw new PluginCatalogException("listing '" + listing.id() + "' needs a version and sdkApiVersion");
        }
        SemanticVersion runtime = SemanticVersion.parse(sdkProperties.getApiVersion());
        if (!listing.sdkApiVersion().isCompatibleWith(runtime)) {
            throw new PluginCatalogException("listing '" + listing.id() + "' targets SDK "
                    + listing.sdkApiVersion() + " incompatible with runtime " + runtime);
        }

        Map<String, PluginListing> versions = byId.computeIfAbsent(listing.id(), k -> new ConcurrentHashMap<>());
        String versionKey = listing.version().toString();
        if (versions.containsKey(versionKey)) {
            throw new PluginCatalogException("plugin '" + listing.id() + "' version "
                    + versionKey + " is already published");
        }
        versions.put(versionKey, listing);
        log.info("[PluginCatalog] published '{}' v{} ({})", listing.id(), versionKey, listing.category());
        return listing;
    }

    /** The latest published version of a plugin, if any. */
    public Optional<PluginListing> find(String id) {
        Map<String, PluginListing> versions = byId.get(id);
        if (versions == null || versions.isEmpty()) {
            return Optional.empty();
        }
        return versions.values().stream().max(Comparator.comparing(PluginListing::version, BY_VERSION));
    }

    /** All published versions of a plugin, oldest first. */
    public List<PluginListing> versions(String id) {
        Map<String, PluginListing> versions = byId.get(id);
        if (versions == null) {
            return List.of();
        }
        return versions.values().stream()
                .sorted(Comparator.comparing(PluginListing::version, BY_VERSION))
                .toList();
    }

    /** The latest listing of every plugin in the catalog. */
    public List<PluginListing> all() {
        List<PluginListing> out = new ArrayList<>();
        for (String id : byId.keySet()) {
            find(id).ifPresent(out::add);
        }
        return out;
    }

    /** Latest listings whose id / name / description contains {@code query} (case-insensitive; blank = all). */
    public List<PluginListing> search(String query) {
        if (query == null || query.isBlank()) {
            return all();
        }
        String q = query.trim().toLowerCase();
        List<PluginListing> out = new ArrayList<>();
        for (PluginListing l : all()) {
            if (contains(l.id(), q) || contains(l.name(), q) || contains(l.description(), q)) {
                out.add(l);
            }
        }
        return out;
    }

    /** Latest listings in a category (case-insensitive). */
    public List<PluginListing> byCategory(String category) {
        List<PluginListing> out = new ArrayList<>();
        for (PluginListing l : all()) {
            if (l.category() != null && l.category().equalsIgnoreCase(category)) {
                out.add(l);
            }
        }
        return out;
    }

    /** Latest listings that declare a capability (case-insensitive). */
    public List<PluginListing> byCapability(String capability) {
        List<PluginListing> out = new ArrayList<>();
        for (PluginListing l : all()) {
            if (l.capabilities().stream().anyMatch(c -> c.equalsIgnoreCase(capability))) {
                out.add(l);
            }
        }
        return out;
    }

    private static boolean contains(String value, String lowercaseQuery) {
        return value != null && value.toLowerCase().contains(lowercaseQuery);
    }
}
