package com.aiqaos.security.rbac;

import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.core.tenant.Tenanted;
import com.aiqaos.security.audit.SecurityAuditEntity;
import com.aiqaos.security.audit.SecurityAuditLogger;
import com.aiqaos.security.audit.SecurityAuditRepository;
import com.aiqaos.security.audit.SecurityEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserSatellitesTenantTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        TenantContextHolder.clear();
    }

    @Test
    void userSessionEntity_implementsTenantedAndCarriesTenantId() {
        UserSessionEntity session = new UserSessionEntity();
        session.setTenantId("acme-corp");
        
        assertThat(session).isInstanceOf(Tenanted.class);
        assertThat(session.getTenantId()).isEqualTo("acme-corp");
    }

    @Test
    void apiKeyEntity_implementsTenantedAndCarriesTenantId() {
        ApiKeyEntity apiKey = new ApiKeyEntity();
        apiKey.setTenantId("beta-team");
        
        assertThat(apiKey).isInstanceOf(Tenanted.class);
        assertThat(apiKey.getTenantId()).isEqualTo("beta-team");
    }

    @Test
    void passwordHistoryEntity_implementsTenantedAndCarriesTenantId() {
        PasswordHistoryEntity history = new PasswordHistoryEntity();
        history.setTenantId("cyber-sec");
        
        assertThat(history).isInstanceOf(Tenanted.class);
        assertThat(history.getTenantId()).isEqualTo("cyber-sec");
    }

    @Test
    void securityAuditLogger_populatesTenantIdFromTenantContextHolder() {
        TenantContextHolder.set(com.aiqaos.core.tenant.TenantContext.ofTenant("tenant-audit-123"));
        
        SecurityAuditRepository repo = mock(SecurityAuditRepository.class);
        SecurityAuditLogger logger = new SecurityAuditLogger(repo);
        
        UUID userId = UUID.randomUUID();
        logger.logEvent(userId, SecurityEventType.LOGIN, "SUCCESS", "127.0.0.1", "test payload");
        
        verify(repo).save(org.mockito.ArgumentMatchers.argThat(audit -> 
            "tenant-audit-123".equals(audit.getTenantId()) &&
            userId.toString().equals(audit.getUserId()) &&
            "LOGIN".equals(audit.getAction())
        ));
    }
}
