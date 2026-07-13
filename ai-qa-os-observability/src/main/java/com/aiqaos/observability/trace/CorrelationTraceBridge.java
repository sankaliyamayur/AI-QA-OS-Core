package com.aiqaos.observability.trace;

import io.opentelemetry.api.trace.Span;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class CorrelationTraceBridge {

    public void bindCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
            Span.current().setAttribute("correlationId", correlationId);
        }
    }

    public void clear() {
        MDC.remove("correlationId");
    }
}