package com.aiqaos.security.audit;

import com.aiqaos.security.event.SecurityEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    public void log(SecurityEvent event) {
        // Future: persist to SecurityAuditEntity and emit to observability
    }
}