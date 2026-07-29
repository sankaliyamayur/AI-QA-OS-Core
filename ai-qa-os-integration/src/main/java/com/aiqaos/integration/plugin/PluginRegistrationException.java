package com.aiqaos.integration.plugin;

/**
 * PLG-1: thrown when a plugin cannot be registered — a duplicate id, an SDK-version incompatibility,
 * or a required permission the runtime has not granted.
 */
public class PluginRegistrationException extends RuntimeException {

    public PluginRegistrationException(String message) {
        super(message);
    }
}
