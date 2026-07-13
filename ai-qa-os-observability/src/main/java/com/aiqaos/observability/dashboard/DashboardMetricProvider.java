package com.aiqaos.observability.dashboard;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class DashboardMetricProvider {

    public Map<String, Object> getRealTimeStats() {
        return Map.of(
            "total_tests",   1250,
            "passed",        1190,
            "failed",        60,
            "quality_score", 0.95
        );
    }
}