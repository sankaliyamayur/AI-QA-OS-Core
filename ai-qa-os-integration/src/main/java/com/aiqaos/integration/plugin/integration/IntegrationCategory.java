package com.aiqaos.integration.plugin.integration;

/**
 * PLG-2: the kind of external system an {@link IntegrationPlugin} connects to — source control,
 * application-lifecycle management, continuous integration, or chat.
 */
public enum IntegrationCategory {
    SCM,
    ALM,
    CI,
    CHAT
}
