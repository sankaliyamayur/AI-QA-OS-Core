package com.aiqaos.gateway.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class GatewayHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
            .withDetail("gateway",     "UP")
            .withDetail("brain",       "UP")
            .withDetail("execution",   "UP")
            .withDetail("reporting",   "UP")
            .withDetail("websocket",   "UP")
            .build();
    }
}