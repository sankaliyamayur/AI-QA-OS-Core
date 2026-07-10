package com.aiqaos.config.system;

import com.aiqaos.config.properties.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    private final SecurityProperties securityProperties;

    public JpaConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(securityProperties.getDefaultAuditor());
    }
}