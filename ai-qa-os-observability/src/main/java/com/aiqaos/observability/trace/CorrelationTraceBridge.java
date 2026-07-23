package com.aiqaos.observability.trace;

import io.opentelemetry.api.trace.Span;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * OBS-1: links logs and traces. Puts {@code correlationId} on both MDC and the current span, and
 * (new) mirrors the current span's {@code traceId} into MDC so every log line can be matched to its
 * trace. Cleared together at the end of a run.
 */
@Component
public class CorrelationTraceBridge {

    public void bindCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
            Span.current().setAttribute("correlationId", correlationId);
        }
    }

    /** Mirror the current span's traceId into MDC (no-op when there is no valid current span). */
    public void bindTraceId() {
        Span current = Span.current();
        if (current.getSpanContext().isValid()) {
            MDC.put("traceId", current.getSpanContext().getTraceId());
        }
    }

    public void clear() {
        MDC.remove("correlationId");
        MDC.remove("traceId");
    }
}
