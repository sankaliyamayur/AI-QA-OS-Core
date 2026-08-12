package com.aiqaos.provider.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * The chain settings must actually bind.
 *
 * <p><b>Why this test exists.</b> {@code ProviderChainProperties} was originally annotated with
 * {@code @ConfigurationProperties} alone. Nothing registered it — no {@code @Component}, and no
 * {@code @EnableConfigurationProperties} listed it — so Spring never created the bean,
 * {@code LLMProviderManager}'s optional injection stayed null, and it fell through to hardcoded
 * defaults. Setting {@code aiqaos.provider.mode} or {@code aiqaos.provider.chain} did nothing at all.
 *
 * <p>It went unnoticed because the hardcoded fallbacks are the safe ones: REAL mode with the standard
 * chain. The failure mode was silent by construction — the documented knobs simply had no effect, and
 * only a test that sets a property and reads it back can catch that.
 */
class ProviderChainPropertiesBindingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(ProviderChainProperties.class);

    @Test
    @DisplayName("the bean exists at all — it previously did not")
    void thePropertiesBeanIsRegistered() {
        runner.run(context -> assertThat(context).hasSingleBean(ProviderChainProperties.class));
    }

    @Test
    void modeBindsFromConfiguration() {
        runner.withPropertyValues("aiqaos.provider.mode=simulated").run(context -> {
            ProviderChainProperties props = context.getBean(ProviderChainProperties.class);
            assertThat(props.resolvedMode()).isEqualTo(ProviderChainProperties.Mode.SIMULATED);
        });
    }

    @Test
    void chainOrderBindsFromConfiguration() {
        runner.withPropertyValues("aiqaos.provider.chain=gemini, claude ,openai").run(context -> {
            ProviderChainProperties props = context.getBean(ProviderChainProperties.class);
            assertThat(props.chainOrder()).containsExactly("gemini", "claude", "openai");
        });
    }

    @Test
    @DisplayName("REAL is the default, so a missing or unknown mode never enables the Simulator")
    void defaultsAreTheSafeOnes() {
        runner.run(context -> {
            ProviderChainProperties props = context.getBean(ProviderChainProperties.class);
            assertThat(props.resolvedMode()).isEqualTo(ProviderChainProperties.Mode.REAL);
            assertThat(props.chainOrder()).isEqualTo(List.of("openai", "claude", "gemini", "ollama"));
        });

        runner.withPropertyValues("aiqaos.provider.mode=nonsense").run(context ->
                assertThat(context.getBean(ProviderChainProperties.class).resolvedMode())
                        .isEqualTo(ProviderChainProperties.Mode.REAL));
    }
}
