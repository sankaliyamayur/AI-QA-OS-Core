package com.aiqaos.healing.strategy;

import com.aiqaos.core.enums.HealingActionType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RecoveryStrategyResolver {

    private final Map<String, String> actionStrategyMap = new HashMap<>();

    public RecoveryStrategyResolver() {
        actionStrategyMap.put("LOCATOR_UPDATE", "LocatorRecoveryStrategy");
        actionStrategyMap.put("SCRIPT_REGENERATE", "ScriptRegenerationStrategy");
        actionStrategyMap.put("WAIT_STRATEGY_CHANGE", "WaitAdjustmentStrategy");
        actionStrategyMap.put("TEST_DATA_UPDATE", "TestDataModificationStrategy");
        actionStrategyMap.put("PROMPT_UPDATE", "PromptOptimizationStrategy");
        actionStrategyMap.put("RETRY_ONLY", "SimpleRetryStrategy");
    }

    public String resolveStrategy(String actionName) {
        if (actionName == null) {
            return "SimpleRetryStrategy";
        }
        return actionStrategyMap.getOrDefault(actionName.toUpperCase().trim(), "SimpleRetryStrategy");
    }

    public String resolveStrategy(HealingActionType actionType) {
        if (actionType == null) {
            return "SimpleRetryStrategy";
        }
        return resolveStrategy(actionType.name());
    }
}
