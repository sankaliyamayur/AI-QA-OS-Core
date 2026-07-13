package com.aiqaos.observability.trace;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TraceContextPropagator {

    private final OpenTelemetry openTelemetry;

    public TraceContextPropagator(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    public Map<String, String> inject(Context context) {
        Map<String, String> carrier = new HashMap<>();
        openTelemetry.getPropagators().getTextMapPropagator().inject(context, carrier, Map::put);
        return carrier;
    }

    public Context extract(Map<String, String> carrier) {
        return openTelemetry.getPropagators().getTextMapPropagator().extract(
            Context.current(),
            carrier,
            new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(Map<String, String> c) { return c.keySet(); }
                @Override
                public String get(Map<String, String> c, String key) { return c.get(key); }
            }
        );
    }
}