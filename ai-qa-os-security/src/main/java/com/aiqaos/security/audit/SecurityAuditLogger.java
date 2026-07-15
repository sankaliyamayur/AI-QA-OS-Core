package com.aiqaos.security.audit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SecurityAuditLogger {

    private final SecurityAuditRepository securityAuditRepository;

    public SecurityAuditLogger(SecurityAuditRepository securityAuditRepository) {
        this.securityAuditRepository = securityAuditRepository;
    }

    @Async
    public void logEvent(UUID userId, SecurityEventType eventType, String result, String ipAddress, String payload) {
        SecurityAuditEntity audit = new SecurityAuditEntity();
        audit.setUserId(userId != null ? userId.toString() : "ANONYMOUS");
        audit.setAction(eventType.name());
        audit.setResult(result);
        audit.setIpAddress(ipAddress);
        audit.setEventTimestamp(LocalDateTime.now());
        audit.setRequestPayload(payload);
        
        securityAuditRepository.save(audit);
    }
}
