package com.aiqaos.dashboard.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA repository/entity scanning and auditing configuration, kept as a plain @Configuration
 * bean (rather than annotations directly on DashboardApplication) so that @WebMvcTest slices
 * - which exclude regular @Configuration beans via their type-exclude-filter but cannot filter
 * annotations placed directly on the discovered @SpringBootConfiguration class - don't try to
 * boot full JPA infrastructure without a datasource.
 */
@Configuration
@EntityScan(basePackages = "com.aiqaos")
@EnableJpaRepositories(basePackages = "com.aiqaos")
@EnableJpaAuditing
public class JpaConfig {
}
