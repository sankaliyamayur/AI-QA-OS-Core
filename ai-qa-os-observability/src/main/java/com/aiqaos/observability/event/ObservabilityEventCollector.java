package com.aiqaos.observability.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ObservabilityEventCollector {

    private final EventProcessor processor;

    public ObservabilityEventCollector(EventProcessor processor) {
        this.processor = processor;
    }

    @EventListener
    public void handleEnvelope(ObservabilityEventEnvelope envelope) {
        processor.process(envelope);
    }
}