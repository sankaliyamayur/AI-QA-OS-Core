package com.aiqaos.integration.plugin.sdk;

import com.aiqaos.core.extension.Extension;
import com.aiqaos.core.extension.ExtensionKind;
import com.aiqaos.integration.plugin.SemanticVersion;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PLG-3: the extension-SDK discovery registry. A custom {@link Extension} (agent / execution engine /
 * reporter / browser) registers here under governance — its id must be unique within its
 * {@link ExtensionKind}, and its declared SDK API version must be compatible with the runtime
 * (reusing PLG-1's {@link SemanticVersion}). Extensions are then discoverable by kind.
 */
@Component
public class ExtensionRegistry {

    private static final Logger log = LoggerFactory.getLogger(ExtensionRegistry.class);

    private final ExtensionSdkProperties properties;
    private final Map<ExtensionKind, Map<String, Extension>> byKind = new ConcurrentHashMap<>();

    public ExtensionRegistry(ExtensionSdkProperties properties) {
        this.properties = properties;
    }

    /**
     * Register an extension. Rejects (with {@link ExtensionRegistrationException}) a duplicate id
     * within the same kind, or an SDK version incompatible with the runtime.
     */
    public synchronized Extension register(Extension extension) {
        if (extension == null) {
            throw new ExtensionRegistrationException("extension is required");
        }
        SemanticVersion runtime = SemanticVersion.parse(properties.getApiVersion());
        SemanticVersion target = SemanticVersion.parse(extension.sdkApiVersion());
        if (!target.isCompatibleWith(runtime)) {
            throw new ExtensionRegistrationException("extension '" + extension.id() + "' targets SDK "
                    + target + " incompatible with runtime " + runtime);
        }

        Map<String, Extension> kindMap = byKind.computeIfAbsent(extension.kind(),
                k -> new ConcurrentHashMap<>());
        if (kindMap.containsKey(extension.id())) {
            throw new ExtensionRegistrationException("duplicate extension id '" + extension.id()
                    + "' for kind " + extension.kind());
        }
        kindMap.put(extension.id(), extension);
        log.info("[ExtensionRegistry] registered {} extension '{}' ({})",
                extension.kind(), extension.id(), extension.extensionPoint());
        return extension;
    }

    /** All registered extensions of a kind. */
    public List<Extension> byKind(ExtensionKind kind) {
        Map<String, Extension> kindMap = byKind.get(kind);
        return kindMap == null ? List.of() : new ArrayList<>(kindMap.values());
    }

    public Optional<Extension> find(ExtensionKind kind, String id) {
        Map<String, Extension> kindMap = byKind.get(kind);
        return Optional.ofNullable(kindMap == null ? null : kindMap.get(id));
    }

    public List<Extension> all() {
        List<Extension> out = new ArrayList<>();
        byKind.values().forEach(m -> out.addAll(m.values()));
        return out;
    }

    /** Kinds that currently have at least one registered extension. */
    public Set<ExtensionKind> kinds() {
        EnumSet<ExtensionKind> present = EnumSet.noneOf(ExtensionKind.class);
        for (Map.Entry<ExtensionKind, Map<String, Extension>> e : byKind.entrySet()) {
            if (!e.getValue().isEmpty()) {
                present.add(e.getKey());
            }
        }
        return present;
    }
}
