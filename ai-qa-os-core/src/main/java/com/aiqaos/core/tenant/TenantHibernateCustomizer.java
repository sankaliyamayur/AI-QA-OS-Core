package com.aiqaos.core.tenant;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * FI-ENT1-C: registers {@link TenantIdentifierResolver} with Hibernate so discriminator multi-tenancy
 * — auto-enabled by the presence of {@code @TenantId} fields on the pilot entities — knows the current
 * tenant (ADR-054). Registering the resolver explicitly here (rather than relying on Spring Boot's
 * bean auto-detection) keeps the behaviour deterministic across both runnable apps that scan
 * {@code com.aiqaos}.
 */
@Component
public class TenantHibernateCustomizer implements HibernatePropertiesCustomizer {

    private final TenantIdentifierResolver resolver = new TenantIdentifierResolver();

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }
}
