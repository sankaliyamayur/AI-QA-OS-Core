package com.aiqaos.integration.plugin.sdk;

/**
 * DX-5 (ADR-079): a clear, author-facing error when a declarative plugin manifest ({@code plugin.json})
 * cannot be parsed or fails validation. The message names the specific problem so a plugin developer
 * can fix their manifest without reading the runtime source.
 */
public class PluginManifestException extends RuntimeException {

    public PluginManifestException(String message) {
        super(message);
    }

    public PluginManifestException(String message, Throwable cause) {
        super(message, cause);
    }
}
