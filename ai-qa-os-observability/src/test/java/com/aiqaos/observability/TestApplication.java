package com.aiqaos.observability;

import com.aiqaos.core.tenant.TenantHibernateCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Minimal bootstrap config so @DataJpaTest can find a @SpringBootConfiguration in this module.
 * FI-ENT1-E: LLMCostEntity now carries @TenantId, so this persistence unit needs the tenant resolver;
 * importing the customizer here supplies it to every @DataJpaTest in the module (ADR-054/056).
 */
@SpringBootApplication
@EnableJpaAuditing
@Import(TenantHibernateCustomizer.class)
public class TestApplication {
}
