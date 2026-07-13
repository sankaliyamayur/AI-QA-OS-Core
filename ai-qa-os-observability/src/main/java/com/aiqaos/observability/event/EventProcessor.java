package com.aiqaos.observability.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventProcessor {
    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    public void process(ObservabilityEventEnvelope envelope) {
        log.info("Processing event: id={}, type={}, correlationId={}", 
            envelope.getEventId(), envelope.getEventType(), envelope.getCorrelationId());
        // Writes to EventEntity repository or pushes to dashboards
    }
}