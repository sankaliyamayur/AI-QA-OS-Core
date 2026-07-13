package com.aiqaos.observability.alert;

import org.springframework.stereotype.Component;

@Component
public class AlertRuleEngine {

    public boolean evaluateThreshold(double value, double threshold) {
        return value > threshold;
    }
}