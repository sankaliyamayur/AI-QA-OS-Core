package com.aiqaos.observability.monitoring;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PlatformHealthAggregator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
            .withDetail("brain_engine",       "HEALTHY")
            .withDetail("execution_engine",   "HEALTHY")
            .withDetail("orchestration_unit", "HEALTHY")
            .build();
    }
}