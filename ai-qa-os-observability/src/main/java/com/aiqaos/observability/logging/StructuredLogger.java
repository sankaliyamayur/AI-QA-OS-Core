package com.aiqaos.observability.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class StructuredLogger {

    private static final Logger log = LoggerFactory.getLogger(StructuredLogger.class);

    public void logInfo(String service, String message, Map<String, Object> details) {
        log.info("StructuredLog: timestamp={}, service={}, message={}, details={}", 
            LocalDateTime.now(), service, message, details);
    }

    public void logError(String service, String message, Throwable err, Map<String, Object> details) {
        log.error("StructuredLog_Error: timestamp={}, service={}, message={}, details={}", 
            LocalDateTime.now(), service, message, details, err);
    }
}