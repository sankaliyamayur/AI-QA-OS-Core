package com.aiqaos.integration.plugin.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.integration.plugin.PluginDescriptor;
import com.aiqaos.integration.plugin.PluginProperties;
import com.aiqaos.integration.plugin.PluginRegistry;
import com.aiqaos.orchestration.plugin.GithubPlugin;
import com.aiqaos.orchestration.plugin.JiraPlugin;
import com.aiqaos.orchestration.plugin.SlackPlugin;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** PLG-2: unit tests for admitting integration plugins into the PLG-1 registry under governance. */
class IntegrationPluginRegistrarTest {

    private List<IntegrationPlugin> allPlugins() {
        return List.of(
                new GitHubIntegrationPlugin(new GithubPlugin()),
                new JiraIntegrationPlugin(new JiraPlugin()),
                new SlackIntegrationPlugin(new SlackPlugin()),
                new GitLabIntegrationPlugin(),
                new AzureDevOpsIntegrationPlugin(),
                new JenkinsIntegrationPlugin(),
                new TeamsIntegrationPlugin());
    }

    private PluginRegistry registryWithGrants(Set<String> grants) {
        PluginProperties props = new PluginProperties();
        props.setSdkApiVersion("1.0.0");
        props.setGrantedPermissions(new LinkedHashSet<>(grants));
        return new PluginRegistry(props);
    }

    @Test
    void admitsAllSevenWhenAllPermissionsGranted() {
        Set<String> allPerms = Set.of("integration.github", "integration.jira", "integration.slack",
                "integration.gitlab", "integration.azure-devops", "integration.jenkins", "integration.teams");
        PluginRegistry registry = registryWithGrants(allPerms);

        int admitted = new IntegrationPluginRegistrar(registry, allPlugins()).registerAll();

        assertThat(admitted).isEqualTo(7);
        assertThat(registry.enabled()).extracting(PluginDescriptor::getId)
                .containsExactlyInAnyOrder("github", "jira", "slack", "gitlab", "azure-devops", "jenkins", "teams");
    }

    @Test
    void skipsPluginsWhosePermissionsAreNotGranted() {
        PluginRegistry registry = registryWithGrants(Set.of("integration.github")); // only github
        int admitted = new IntegrationPluginRegistrar(registry, allPlugins()).registerAll();

        assertThat(admitted).isEqualTo(1);
        assertThat(registry.enabled()).extracting(PluginDescriptor::getId).containsExactly("github");
    }

    @Test
    void noGrantsMeansIntegrationsOffByDefault() {
        PluginRegistry registry = registryWithGrants(Set.of());
        assertThat(new IntegrationPluginRegistrar(registry, allPlugins()).registerAll()).isZero();
        assertThat(registry.enabled()).isEmpty();
    }
}
