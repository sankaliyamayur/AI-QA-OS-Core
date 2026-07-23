package com.aiqaos.observability.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * OBS-1: proves the cross-boundary propagation mechanism — a trace context injected into a header
 * carrier extracts back to the same trace. This is what carries a trace across the dashboard↔gateway
 * hop once the boundary filters are wired (deferred, needs both apps + the collector).
 */
class TraceContextPropagatorTest {

    private final SdkTracerProvider provider = SdkTracerProvider.builder().build();
    private final OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(provider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();
    private final TraceContextPropagator propagator = new TraceContextPropagator(openTelemetry);

    @Test
    void injectThenExtractRoundTripsTheTraceId() {
        Tracer tracer = openTelemetry.getTracer("test");
        Span span = tracer.spanBuilder("op").startSpan();

        String originalTraceId;
        Map<String, String> carrier;
        try (Scope scope = span.makeCurrent()) {
            originalTraceId = span.getSpanContext().getTraceId();
            carrier = propagator.inject(Context.current());
        } finally {
            span.end();
        }

        assertThat(carrier).containsKey("traceparent");

        Context extracted = propagator.extract(carrier);
        String extractedTraceId = Span.fromContext(extracted).getSpanContext().getTraceId();
        assertThat(extractedTraceId).isEqualTo(originalTraceId);
    }
}
