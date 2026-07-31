package com.aiqaos.core.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * SCALE-2 (ADR-064): the distributed {@link EventBus} binding. A {@link BaseEvent} published on any
 * instance is serialised into an {@link EventEnvelope} (carrying the concrete class FQN) onto the
 * {@code aiqaos.events} topic; every instance's {@code @KafkaListener} deserialises it and dispatches
 * to its local subscribers via the shared {@link EventDispatch}. A per-instance consumer group makes
 * this a broadcast (every instance receives every event) — cross-JVM coordination over the same seam.
 *
 * <p>Active only when {@code aiqaos.events.transport=kafka} AND {@code spring-kafka} is on the classpath
 * (an app opts in); otherwise {@link InProcessEventBus} is the default. {@code @ConditionalOnClass}
 * (evaluated via bytecode, so the class is never loaded when absent) lets {@code core} declare
 * {@code spring-kafka} as an <em>optional</em> dependency with no hard Kafka weight on other modules.
 */
@Component
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name = "aiqaos.events.transport", havingValue = "kafka")
public class KafkaEventBus implements EventBus {

    static final String TOPIC = "aiqaos.events";
    private static final Logger log = LoggerFactory.getLogger(KafkaEventBus.class);

    private final EventDispatch dispatch = new EventDispatch();
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventBus(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T extends BaseEvent> void subscribe(Class<T> type, Consumer<T> handler) {
        dispatch.subscribe(type, handler);
    }

    @Override
    public void publish(BaseEvent event) {
        if (event == null) {
            return;
        }
        String message = serialize(event);
        if (message != null) {
            // key = event type → per-type partition affinity / ordering
            kafkaTemplate.send(TOPIC, event.getClass().getSimpleName(), message);
        }
    }

    /** Broadcast: a per-instance group id so every instance receives every event. */
    @KafkaListener(topics = TOPIC, groupId = "${aiqaos.events.group-id:aiqaos-events-${random.uuid}}")
    void onMessage(String message) {
        receive(message);
    }

    /** Serialise an event into the wire envelope JSON. Package-private for unit testing. */
    String serialize(BaseEvent event) {
        try {
            EventEnvelope envelope = new EventEnvelope(event.getClass().getName(),
                    objectMapper.writeValueAsString(event));
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            log.error("Failed to serialise event {}: {}", event.getClass().getSimpleName(), e.toString());
            return null;
        }
    }

    /** Deserialise a wire message to its concrete {@link BaseEvent} subtype and dispatch it locally. */
    void receive(String message) {
        try {
            EventEnvelope envelope = objectMapper.readValue(message, EventEnvelope.class);
            Class<?> clazz = Class.forName(envelope.getType());
            if (!BaseEvent.class.isAssignableFrom(clazz)) {
                log.warn("Ignoring non-BaseEvent message type: {}", envelope.getType());
                return;
            }
            BaseEvent event = (BaseEvent) objectMapper.readValue(envelope.getPayload(), clazz);
            dispatch.dispatch(event);
        } catch (Exception e) {
            log.warn("Failed to handle event message: {}", e.toString());
        }
    }
}
