package com.aiqaos.observability.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OBS-1: wires the OTel SDK end-to-end. Spans are attributed to {@code service.name=ai-qa-os} and
 * exported to the OTel Collector <b>only when opted in</b> ({@code aiqaos.otel.exporter=otlp} +
 * {@code aiqaos.otel.endpoint}); otherwise there is no span processor and spans are dropped
 * (no-op default — non-breaking, near-zero overhead, and runnable without a collector). ADR-020.
 */
@Configuration
public class TelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry(
            @Value("${aiqaos.otel.exporter:none}") String exporter,
            @Value("${aiqaos.otel.endpoint:http://localhost:4317}") String endpoint) {

        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), "ai-qa-os")));

        SdkTracerProviderBuilder providerBuilder = SdkTracerProvider.builder().setResource(resource);
        if ("otlp".equalsIgnoreCase(exporter)) {
            OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();
            providerBuilder.addSpanProcessor(BatchSpanProcessor.builder(otlpExporter).build());
        }
        SdkTracerProvider tracerProvider = providerBuilder.build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("ai-qa-os-tracer", "1.0.0");
    }
}
