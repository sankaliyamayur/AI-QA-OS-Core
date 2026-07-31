package com.aiqaos.core.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * SCALE-2 (ADR-060): the in-process coordination bus dispatches by type (and supertype), runs all
 * handlers, and isolates a throwing one. No infrastructure.
 */
class InProcessEventBusTest {

    /** Self-contained concrete events (BaseEvent is abstract). */
    static class TestWorkflowEvent extends BaseEvent {}

    static class TestExecutionEvent extends BaseEvent {}

    @Test
    void dispatchesToTypeAndSupertypeHandlersOnly() {
        InProcessEventBus bus = new InProcessEventBus();
        AtomicInteger baseSeen = new AtomicInteger();
        AtomicInteger workflowSeen = new AtomicInteger();
        AtomicInteger executionSeen = new AtomicInteger();

        bus.subscribe(BaseEvent.class, e -> baseSeen.incrementAndGet());
        bus.subscribe(TestWorkflowEvent.class, e -> workflowSeen.incrementAndGet());
        bus.subscribe(TestExecutionEvent.class, e -> executionSeen.incrementAndGet());

        bus.publish(new TestWorkflowEvent());

        assertEquals(1, workflowSeen.get(), "the workflow handler sees the workflow event");
        assertEquals(1, baseSeen.get(), "the BaseEvent handler sees it too (supertype)");
        assertEquals(0, executionSeen.get(), "the execution handler must NOT see a workflow event");
    }

    @Test
    void allHandlersRunAndAThrowingOneIsIsolated() {
        InProcessEventBus bus = new InProcessEventBus();
        AtomicInteger reached = new AtomicInteger();
        bus.subscribe(TestWorkflowEvent.class, e -> { throw new RuntimeException("boom"); });
        bus.subscribe(TestWorkflowEvent.class, e -> reached.incrementAndGet());

        assertDoesNotThrow(() -> bus.publish(new TestWorkflowEvent()));
        assertEquals(1, reached.get(), "a throwing handler must not stop the others");
    }

    @Test
    void nullEventIsIgnored() {
        InProcessEventBus bus = new InProcessEventBus();
        assertDoesNotThrow(() -> bus.publish(null));
    }
}
