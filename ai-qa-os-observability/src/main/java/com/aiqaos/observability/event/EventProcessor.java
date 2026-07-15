package com.aiqaos.observability.event;

import com.aiqaos.observability.entity.EventEntity;
import com.aiqaos.observability.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventProcessor {
    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    private final EventRepository eventRepository;

    public EventProcessor(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void process(ObservabilityEventEnvelope envelope) {
        log.info("Processing event: id={}, type={}, correlationId={}",
            envelope.getEventId(), envelope.getEventType(), envelope.getCorrelationId());

        EventEntity entity = new EventEntity();
        entity.setEventType(envelope.getEventType());
        entity.setSource(envelope.getSource());
        entity.setPayload(envelope.getPayload());
        entity.setCreatedAt(LocalDateTime.now());
        eventRepository.save(entity);
    }
}