package com.aiqaos.observability.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AlertManager {
    private static final Logger log = LoggerFactory.getLogger(AlertManager.class);

    private final AlertRuleEngine ruleEngine;

    public AlertManager(AlertRuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    public void processAlert(String ruleName, double currentVal, double thresholdLimit) {
        if (ruleEngine.evaluateThreshold(currentVal, thresholdLimit)) {
            log.warn("ALERT TRIGGERED: rule={}, value={}, threshold={}", ruleName, currentVal, thresholdLimit);
        }
    }
}