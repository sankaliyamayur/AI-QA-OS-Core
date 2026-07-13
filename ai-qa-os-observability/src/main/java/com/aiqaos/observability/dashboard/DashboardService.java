package com.aiqaos.observability.dashboard;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service("observabilityDashboardService")
public class DashboardService {

    private final DashboardMetricProvider metricProvider;

    public DashboardService(DashboardMetricProvider metricProvider) {
        this.metricProvider = metricProvider;
    }

    public Map<String, Object> getDashboardData() {
        return metricProvider.getRealTimeStats();
    }
}