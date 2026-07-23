package com.aiqaos.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.observability.trace.TraceContextPropagator;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * OBS-1 (completion): proves the inbound side of cross-JVM tracing — an incoming traceparent becomes
 * the current span context for the request, so downstream workflow spans join that trace.
 */
class TracingFilterTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String TRACEPARENT = "00-" + TRACE_ID + "-00f067aa0ba902b7-01";

    private final OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder().build())
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();

    private final TracingFilter filter = new TracingFilter(new TraceContextPropagator(openTelemetry));

    @Test
    void continuesAnIncomingTraceInsideTheRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("traceparent", TRACEPARENT);
        String[] seenTraceId = new String[1];

        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> seenTraceId[0] = Span.current().getSpanContext().getTraceId());

        assertThat(seenTraceId[0]).isEqualTo(TRACE_ID);
    }

    @Test
    void aRequestWithoutTraceparentStillProceeds() throws Exception {
        boolean[] chainCalled = {false};

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> chainCalled[0] = true);

        assertThat(chainCalled[0]).isTrue();
    }
}
