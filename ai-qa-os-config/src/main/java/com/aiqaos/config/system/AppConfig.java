package com.aiqaos.config.system;

import com.aiqaos.config.properties.ApplicationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
@EnableConfigurationProperties({
    ApplicationProperties.class,
    com.aiqaos.config.properties.DatabaseProperties.class,
    com.aiqaos.config.properties.WorkflowProperties.class,
    com.aiqaos.config.properties.MemoryProperties.class,
    com.aiqaos.config.properties.PromptProperties.class,
    com.aiqaos.config.properties.ExecutionProperties.class,
    com.aiqaos.config.properties.MonitoringProperties.class,
    com.aiqaos.config.properties.CacheProperties.class,
    com.aiqaos.config.properties.SecurityProperties.class,
    com.aiqaos.config.properties.FeatureFlagProperties.class,
    com.aiqaos.config.properties.AiProviderProperties.class
})
public class AppConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}