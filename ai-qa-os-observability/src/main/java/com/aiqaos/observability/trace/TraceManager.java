package com.aiqaos.observability.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Component;

@Component
public class TraceManager {

    private final Tracer tracer;

    public TraceManager(Tracer tracer) {
        this.tracer = tracer;
    }

    public Span startSpan(String name) {
        return tracer.spanBuilder(name).startSpan();
    }

    public void endSpan(Span span) {
        if (span != null) {
            span.end();
        }
    }
}