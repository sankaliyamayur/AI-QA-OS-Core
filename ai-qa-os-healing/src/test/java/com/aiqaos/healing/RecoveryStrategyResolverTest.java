package com.aiqaos.healing;

import com.aiqaos.core.enums.HealingActionType;
import com.aiqaos.healing.strategy.RecoveryStrategyResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecoveryStrategyResolverTest {

    private final RecoveryStrategyResolver resolver = new RecoveryStrategyResolver();

    @Test
    void testResolveStrategyFromString() {
        assertEquals("LocatorRecoveryStrategy", resolver.resolveStrategy("LOCATOR_UPDATE"));
        assertEquals("ScriptRegenerationStrategy", resolver.resolveStrategy("SCRIPT_REGENERATE"));
        assertEquals("WaitAdjustmentStrategy", resolver.resolveStrategy("WAIT_STRATEGY_CHANGE"));
        assertEquals("SimpleRetryStrategy", resolver.resolveStrategy("RETRY_ONLY"));
        assertEquals("SimpleRetryStrategy", resolver.resolveStrategy((String) null));
    }

    @Test
    void testResolveStrategyFromEnum() {
        assertEquals("LocatorRecoveryStrategy", resolver.resolveStrategy(HealingActionType.LOCATOR_UPDATE));
        assertEquals("ScriptRegenerationStrategy", resolver.resolveStrategy(HealingActionType.SCRIPT_REGENERATE));
        assertEquals("WaitAdjustmentStrategy", resolver.resolveStrategy(HealingActionType.WAIT_STRATEGY_CHANGE));
        assertEquals("SimpleRetryStrategy", resolver.resolveStrategy((HealingActionType) null));
    }
}
