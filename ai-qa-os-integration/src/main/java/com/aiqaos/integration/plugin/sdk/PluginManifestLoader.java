package com.aiqaos.integration.plugin.sdk;

import com.aiqaos.integration.plugin.PluginManifest;
import com.aiqaos.integration.plugin.SemanticVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * DX-5 (ADR-079): the developer-facing entry point of the plugin SDK — load a plugin's metadata from a
 * declarative {@code plugin.json} instead of hand-constructing {@link PluginManifest} in Java. Parses
 * and <b>validates</b> the manifest (id present; {@code version}/{@code sdkApiVersion} parseable via
 * PLG-1's {@link SemanticVersion}) into a {@link PluginManifest} ready for
 * {@code PluginRegistry.register(plugin, manifest)}. Any problem raises a {@link PluginManifestException}
 * naming the specific field.
 *
 * <p>Schema:
 * <pre>{@code
 * { "id": "com.acme.my-plugin", "version": "1.2.0", "sdkApiVersion": "1.0.0",
 *   "capabilities": ["report.export"], "requiredPermissions": ["network.http"] }
 * }</pre>
 */
@Component
public class PluginManifestLoader {

    private final ObjectMapper objectMapper;

    public PluginManifestLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Load + validate a manifest from a JSON string. */
    public PluginManifest load(String json) {
        if (json == null || json.isBlank()) {
            throw new PluginManifestException("plugin manifest JSON is empty");
        }
        try {
            return fromNode(objectMapper.readTree(json));
        } catch (PluginManifestException e) {
            throw e;
        } catch (Exception e) {
            throw new PluginManifestException("could not parse plugin manifest JSON: " + e.getMessage(), e);
        }
    }

    /** Load + validate a manifest from a stream (e.g. a file or classpath resource). */
    public PluginManifest load(InputStream json) {
        try {
            return fromNode(objectMapper.readTree(json));
        } catch (PluginManifestException e) {
            throw e;
        } catch (Exception e) {
            throw new PluginManifestException("could not read plugin manifest: " + e.getMessage(), e);
        }
    }

    /** Load + validate {@code plugin.json} from the classpath — the common plugin-author path. */
    public PluginManifest loadFromClasspath(String resource) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new PluginManifestException("plugin manifest not found on classpath: " + resource);
            }
            return load(in);
        } catch (IOException e) {
            throw new PluginManifestException("could not read classpath manifest '" + resource + "': " + e.getMessage(), e);
        }
    }

    private PluginManifest fromNode(JsonNode node) {
        if (node == null || !node.isObject()) {
            throw new PluginManifestException("plugin manifest must be a JSON object");
        }
        String id = text(node, "id");
        if (id == null || id.isBlank()) {
            throw new PluginManifestException("plugin manifest 'id' is required");
        }
        SemanticVersion version = version(node, "version");
        SemanticVersion sdkApiVersion = version(node, "sdkApiVersion");
        Set<String> capabilities = stringSet(node, "capabilities");
        Set<String> requiredPermissions = stringSet(node, "requiredPermissions");
        try {
            return new PluginManifest(id, version, sdkApiVersion, capabilities, requiredPermissions);
        } catch (IllegalArgumentException e) {
            throw new PluginManifestException("invalid plugin manifest: " + e.getMessage(), e);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }

    private SemanticVersion version(JsonNode node, String field) {
        String raw = text(node, field);
        if (raw == null || raw.isBlank()) {
            throw new PluginManifestException("plugin manifest '" + field + "' is required");
        }
        try {
            return SemanticVersion.parse(raw);
        } catch (RuntimeException e) {
            throw new PluginManifestException(
                    "plugin manifest '" + field + "' is not a valid semantic version: '" + raw + "'", e);
        }
    }

    private static Set<String> stringSet(JsonNode node, String field) {
        Set<String> out = new LinkedHashSet<>();
        JsonNode array = node.get(field);
        if (array != null && array.isArray()) {
            for (JsonNode element : array) {
                String s = element.asText(null);
                if (s != null && !s.isBlank()) {
                    out.add(s.trim());
                }
            }
        }
        return out;
    }
}
