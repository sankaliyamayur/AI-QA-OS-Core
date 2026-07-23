package com.aiqaos.gateway.filter;

import com.aiqaos.observability.trace.TraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * OBS-1 (completion): extracts an inbound W3C {@code traceparent} so a trace started upstream —
 * e.g. in the dashboard when it proxies an approve/reject — continues through the gateway, with the
 * workflow's run/step spans nesting under it instead of starting a disconnected trace.
 *
 * <p>Ordered ahead of {@link CorrelationIdFilter} ({@code @Order(1)}) so the extracted span context
 * is current for the entire request. A request without a {@code traceparent} simply proceeds.
 */
@Component
@Order(0)
public class TracingFilter implements Filter {

    private final TraceContextPropagator propagator;

    public TracingFilter(TraceContextPropagator propagator) {
        this.propagator = propagator;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Context extracted = propagator.extract(headersOf((HttpServletRequest) request));
        try (Scope scope = extracted.makeCurrent()) {
            chain.doFilter(request, response);
        }
    }

    /** Header names are lower-cased: W3C propagation looks up the exact key {@code traceparent}. */
    private static Map<String, String> headersOf(HttpServletRequest request) {
        Map<String, String> carrier = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                carrier.put(name.toLowerCase(), request.getHeader(name));
            }
        }
        return carrier;
    }
}
