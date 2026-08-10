package com.aiqaos.execution.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.execution.artifact.ArtifactStore;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ENT-5 (FI-ENT5-F): Spring wiring for the retention trigger. The plain unit tests prove the sweep
 * logic; these prove the bean actually materialises (or stays absent) under real property binding —
 * the failure mode that bit ENT-5's S3 opt-in, where correct code was simply never wire-able in a
 * running app.
 */
class ArtifactRetentionSchedulerWiringTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
            .withUserConfiguration(StoreConfig.class,
                    ArtifactRetentionService.class, ArtifactRetentionScheduler.class);

    @Test
    void notRegisteredByDefault() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            // default deployments must be byte-for-byte unchanged — no timer, no purge
            assertThat(context).doesNotHaveBean(ArtifactRetentionScheduler.class);
        });
    }

    @Test
    void notRegisteredWhenExplicitlyDisabled() {
        runner.withPropertyValues("aiqaos.artifacts.retention.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(ArtifactRetentionScheduler.class));
    }

    @Test
    void registeredAndArmedWhenEnabled() {
        runner.withPropertyValues("aiqaos.artifacts.retention.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ArtifactRetentionScheduler.class);
                    // the default daily cron must be a valid expression at startup
                });
    }

    @Test
    void honoursAnExplicitCron() {
        runner.withPropertyValues(
                        "aiqaos.artifacts.retention.enabled=true",
                        "aiqaos.artifacts.retention.cron=0 30 4 * * *")
                .run(context -> assertThat(context).hasSingleBean(ArtifactRetentionScheduler.class));
    }

    @Test
    void cronDisabledStillRegistersTheOnDemandBean() {
        runner.withPropertyValues(
                        "aiqaos.artifacts.retention.enabled=true",
                        "aiqaos.artifacts.retention.cron=-")
                .run(context -> assertThat(context).hasSingleBean(ArtifactRetentionScheduler.class));
    }

    @Test
    void malformedCronFailsContextStartup() {
        runner.withPropertyValues(
                        "aiqaos.artifacts.retention.enabled=true",
                        "aiqaos.artifacts.retention.cron=every-tuesday")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void bindsATenantSourceWhenOneIsOnTheClasspath() {
        runner.withUserConfiguration(TenantSourceConfig.class)
                .withPropertyValues(
                        "aiqaos.artifacts.retention.enabled=true",
                        "aiqaos.artifacts.retention.cron=-")
                .run(context -> {
                    assertThat(context).hasSingleBean(RetentionTenantSource.class);
                    // the ObjectProvider must resolve it — otherwise only __system__ would ever be swept
                    assertThat(context.getBean(ArtifactRetentionScheduler.class).purgeAllTenants()).isZero();
                    assertThat(context.getBean(RecordingTenantSource.class).asked).isTrue();
                });
    }

    // --- test configuration ----------------------------------------------------------------------

    @Configuration(proxyBeanMethods = false)
    static class StoreConfig {
        @Bean
        ArtifactStore artifactStore() {
            return new EmptyArtifactStore();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TenantSourceConfig {
        @Bean
        RecordingTenantSource recordingTenantSource() {
            return new RecordingTenantSource();
        }
    }

    static class RecordingTenantSource implements RetentionTenantSource {
        boolean asked;

        @Override
        public List<String> tenantIds() {
            asked = true;
            return List.of("acme");
        }
    }

    private static final class EmptyArtifactStore implements ArtifactStore {
        @Override public String store(String key, byte[] content) { return key; }
        @Override public byte[] resolve(String key) { throw new NoSuchElementException(key); }
        @Override public boolean exists(String key) { return false; }
        @Override public List<String> list(String prefix) { return List.of(); }
        @Override public void delete(String key) { }
        @Override public Instant lastModified(String key) { throw new NoSuchElementException(key); }
    }
}
