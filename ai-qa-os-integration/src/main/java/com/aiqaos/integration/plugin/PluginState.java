package com.aiqaos.integration.plugin;

/**
 * PLG-1: a plugin's lifecycle state. Valid transitions: {@code REGISTERED → INITIALIZED → ENABLED ⇄
 * DISABLED}; {@code FAILED} is terminal for a plugin whose hook threw.
 */
public enum PluginState {
    REGISTERED,
    INITIALIZED,
    ENABLED,
    DISABLED,
    FAILED
}
