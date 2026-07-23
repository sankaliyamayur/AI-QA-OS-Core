package com.aiqaos.observability.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * OBS-1: proves the span instrumentation without a collector — spans are captured by OTel's
 * in-memory exporter, carry the correlationId attribute, and expose their traceId to MDC.
 */
class TracingInstrumentationTest {

    private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
    private final SdkTracerProvider provider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build();
    private final Tracer tracer = OpenTelemetrySdk.builder()
            .setTracerProvider(provider)
            .build()
            .getTracer("test");
    private final CorrelationTraceBridge bridge = new CorrelationTraceBridge();

    @AfterEach
    void tearDown() {
        MDC.clear();
        provider.close();
    }

    @Test
    void spanCarriesCorrelationIdAndTraceIdGoesToMdc() {
        Span span = tracer.spanBuilder("workflow.run").startSpan();
        String traceId;
        try (Scope scope = span.makeCurrent()) {
            bridge.bindCorrelationId("corr-123");
            bridge.bindTraceId();
            traceId = span.getSpanContext().getTraceId();

            assertThat(MDC.get("correlationId")).isEqualTo("corr-123");
            assertThat(MDC.get("traceId")).isEqualTo(traceId);
        } finally {
            span.end();
        }

        bridge.clear();
        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("traceId")).isNull();

        assertThat(exporter.getFinishedSpanItems()).hasSize(1);
        SpanData data = exporter.getFinishedSpanItems().get(0);
        assertThat(data.getName()).isEqualTo("workflow.run");
        assertThat(data.getAttributes().get(AttributeKey.stringKey("correlationId"))).isEqualTo("corr-123");
    }
}
