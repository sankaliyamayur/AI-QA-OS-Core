package com.aiqaos.integration.plugin.sdk;

/**
 * PLG-3: thrown when an {@code Extension} cannot be registered — a duplicate id within its kind, or
 * an SDK API version incompatible with the runtime.
 */
public class ExtensionRegistrationException extends RuntimeException {

    public ExtensionRegistrationException(String message) {
        super(message);
    }
}
