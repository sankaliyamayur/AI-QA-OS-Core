package com.aiqaos.integration.plugin.marketplace;

/**
 * PLG-4 (ADR-080): a publish rejection from the {@link PluginCatalog} — a blank id/name, an
 * SDK-incompatible listing, or a re-publish of an already-published version. Author-facing.
 */
public class PluginCatalogException extends RuntimeException {

    public PluginCatalogException(String message) {
        super(message);
    }
}
