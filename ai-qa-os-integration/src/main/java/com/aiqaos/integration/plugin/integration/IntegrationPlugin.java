package com.aiqaos.integration.plugin.integration;

import com.aiqaos.integration.plugin.Plugin;
import com.aiqaos.integration.plugin.PluginManifest;

/**
 * PLG-2: an integration expressed as a first-class PLG-1 {@link Plugin}. Adds its {@link PluginManifest}
 * (for governed registration), its {@link IntegrationCategory}, and a uniform {@code execute} action
 * surface — so GitHub, Jira, Slack, GitLab, Azure DevOps, Jenkins, and Teams all sit behind one
 * contract.
 */
public interface IntegrationPlugin extends Plugin {

    PluginManifest manifest();

    IntegrationCategory category();

    IntegrationResponse execute(IntegrationRequest request);
}
