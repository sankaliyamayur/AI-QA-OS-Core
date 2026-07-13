package com.aiqaos.observability.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.springframework.stereotype.Component;

@Component
public class SpanManager {

    private final Tracer tracer;

    public SpanManager(Tracer tracer) {
        this.tracer = tracer;
    }

    public Span startChildSpan(String parentName, String childName) {
        Span parentSpan = Span.current();
        return tracer.spanBuilder(childName)
            .setParent(Context.current().with(parentSpan))
            .startSpan();
    }
}