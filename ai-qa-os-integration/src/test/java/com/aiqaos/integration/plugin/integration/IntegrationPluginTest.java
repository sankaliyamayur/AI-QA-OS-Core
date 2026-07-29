package com.aiqaos.integration.plugin.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.integration.plugin.PluginManifest;
import com.aiqaos.orchestration.plugin.GithubPlugin;
import org.junit.jupiter.api.Test;

/** PLG-2: unit tests for the integration plugins' execute + manifest (delegation and simulation). */
class IntegrationPluginTest {

    @Test
    void gitHubDelegatesToExistingPlugin() {
        GitHubIntegrationPlugin plugin = new GitHubIntegrationPlugin(new GithubPlugin());
        IntegrationResponse r = plugin.execute(IntegrationRequest.of("commit", "abc123"));
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.getMessage()).contains("Github").contains("abc123"); // delegated output
        assertThat(plugin.category()).isEqualTo(IntegrationCategory.SCM);
    }

    @Test
    void newPluginSimulatesAction() {
        IntegrationResponse r = new GitLabIntegrationPlugin().execute(IntegrationRequest.of("trigger", "pipeline-7"));
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.getMessage()).contains("gitlab").contains("trigger").contains("pipeline-7");
    }

    @Test
    void categoriesAreAssignedCorrectly() {
        assertThat(new JiraIntegrationPlugin(new com.aiqaos.orchestration.plugin.JiraPlugin()).category())
                .isEqualTo(IntegrationCategory.ALM);
        assertThat(new AzureDevOpsIntegrationPlugin().category()).isEqualTo(IntegrationCategory.CI);
        assertThat(new JenkinsIntegrationPlugin().category()).isEqualTo(IntegrationCategory.CI);
        assertThat(new TeamsIntegrationPlugin().category()).isEqualTo(IntegrationCategory.CHAT);
    }

    @Test
    void manifestIsWellFormed() {
        PluginManifest m = new GitHubIntegrationPlugin(new GithubPlugin()).manifest();
        assertThat(m.getId()).isEqualTo("github");
        assertThat(m.getSdkApiVersion().toString()).isEqualTo("1.0.0");
        assertThat(m.getCapabilities()).contains("scm.commit");
        assertThat(m.getRequiredPermissions()).containsExactly("integration.github");
    }
}
