package com.aiqaos.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * TestApplication — Spring Boot bootstrap for Phase 18 integration tests.
 * Scans the entire com.aiqaos namespace for all modules.
 * Explicitly enables JPA repos and entity scanning since multiple Spring Data modules
 * (JPA + Redis) are on the classpath, requiring strict mode configuration.
 */
@SpringBootApplication(scanBasePackages = {"com.aiqaos"})
@EntityScan(basePackages = {"com.aiqaos"})
@EnableJpaRepositories(basePackages = {"com.aiqaos"})
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}