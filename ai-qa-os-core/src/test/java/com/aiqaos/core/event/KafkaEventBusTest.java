package com.aiqaos.core.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * SCALE-2 (ADR-064): the distributed bus's wire logic — serialise → (Kafka) → receive → dispatch —
 * proven without a live broker. The envelope carries the concrete FQN so the consumer reconstructs the
 * exact {@link BaseEvent} subtype and the payload round-trips. Mockito-free (JDK 25).
 */
class KafkaEventBusTest {

    private KafkaEventBus newBus() {
        // KafkaTemplate is constructed but never used (we exercise serialize/receive, not publish),
        // so a no-op ProducerFactory proxy is sufficient — no broker, no producer.
        @SuppressWarnings("unchecked")
        ProducerFactory<String, String> pf = (ProducerFactory<String, String>) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{ProducerFactory.class}, (p, m, a) -> {
                    // KafkaTemplate's constructor calls transactionCapable() (a boolean) — must not be null.
                    Class<?> rt = m.getReturnType();
                    if (rt == boolean.class || rt == Boolean.class) {
                        return false;
                    }
                    return null;
                });
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        return new KafkaEventBus(new KafkaTemplate<>(pf), mapper);
    }

    @Test
    void serializeThenReceive_reconstructsExactTypeAndDispatches() {
        KafkaEventBus bus = newBus();
        AtomicReference<WorkflowEvent> workflowGot = new AtomicReference<>();
        AtomicReference<ExecutionEvent> executionGot = new AtomicReference<>();
        bus.subscribe(WorkflowEvent.class, workflowGot::set);
        bus.subscribe(ExecutionEvent.class, executionGot::set);

        WorkflowEvent event = new WorkflowEvent();
        event.getMetadata().setTenantId("acme");

        String message = bus.serialize(event);
        assertNotNull(message, "serialisation produces a wire message");
        assertTrue(message.contains(WorkflowEvent.class.getName()), "envelope carries the concrete FQN");

        bus.receive(message);

        assertNotNull(workflowGot.get(), "a WorkflowEvent subscriber receives the reconstructed event");
        assertEquals("acme", workflowGot.get().getMetadata().getTenantId(), "payload round-trips");
        assertNull(executionGot.get(), "an ExecutionEvent subscriber does NOT receive a WorkflowEvent");
    }

    @Test
    void baseEventSubscriberReceivesAnySeamEvent() {
        KafkaEventBus bus = newBus();
        AtomicReference<BaseEvent> any = new AtomicReference<>();
        bus.subscribe(BaseEvent.class, any::set);

        bus.receive(bus.serialize(new ExecutionEvent()));

        assertNotNull(any.get(), "a BaseEvent subscriber sees events of any subtype (supertype dispatch)");
    }

    @Test
    void malformedMessageIsIgnored() {
        KafkaEventBus bus = newBus();
        bus.subscribe(BaseEvent.class, e -> {
            throw new AssertionError("no handler should fire for a bad message");
        });
        bus.receive("not-json");        // must not throw
        bus.receive("{\"type\":\"java.lang.String\",\"payload\":\"x\"}"); // non-BaseEvent type → ignored
    }
}
