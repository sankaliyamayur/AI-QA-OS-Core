package com.aiqaos.integration.coordinator;

import com.aiqaos.integration.context.IntegrationContext;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class PlatformIntegrationManager {

    private final ThreadLocal<IntegrationContext> contextHolder = new ThreadLocal<>();

    public IntegrationContext establishContext(String userId) {
        IntegrationContext context = new IntegrationContext(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            userId,
            UUID.randomUUID().toString()
        );
        contextHolder.set(context);
        return context;
    }

    public IntegrationContext getContext() {
        return contextHolder.get();
    }

    public void clearContext() {
        contextHolder.remove();
    }
}