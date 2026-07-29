package com.aiqaos.provider.provider.simulator;

import com.aiqaos.provider.contract.ProviderCapability;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimulatorProviderTest {

    private SimulatorProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SimulatorProvider();
    }

    @Test
    @DisplayName("DX-3: SimulatorProvider should return true for availability & capability support")
    void testProviderCapabilities() {
        assertEquals("simulator", provider.getProviderName());
        assertTrue(provider.isAvailable());
        assertTrue(provider.supports(ProviderCapability.CHAT));
        assertTrue(provider.supports(ProviderCapability.CODE_GENERATION));
    }

    @Test
    @DisplayName("DX-3: Should return locator extraction JSON when prompt contains 'locator'")
    void testLocatorPromptResponse() {
        LLMRequest req = new LLMRequest("Extract locators for login form", "local-simulator", "locators");
        LLMResponse resp = provider.generate(req);

        assertNotNull(resp);
        assertEquals("local-simulator-v1", resp.getModel());
        assertTrue(resp.getText().contains("targetLocators"));
        assertTrue(resp.getText().contains("usernameInput"));
    }

    @Test
    @DisplayName("DX-3: Should return Playwright script code when prompt contains 'playwright'")
    void testPlaywrightPromptResponse() {
        LLMRequest req = new LLMRequest("Generate playwright test script for user flow", "local-simulator", "script");
        LLMResponse resp = provider.generate(req);

        assertNotNull(resp);
        assertTrue(resp.getText().contains("import { test, expect } from '@playwright/test';"));
    }

    @Test
    @DisplayName("DX-3: Should return self-healing JSON when prompt contains 'heal'")
    void testSelfHealingPromptResponse() {
        LLMRequest req = new LLMRequest("Heal broken locator button#submit-old", "local-simulator", "healing");
        LLMResponse resp = provider.generate(req);

        assertNotNull(resp);
        assertTrue(resp.getText().contains("\"healed\": true"));
        assertTrue(resp.getText().contains("repairedSelector"));
    }

    @Test
    @DisplayName("DX-3: Should return requirement scenarios JSON when prompt contains 'requirement'")
    void testRequirementPromptResponse() {
        LLMRequest req = new LLMRequest("Analyze requirement US-101 story", "local-simulator", "analysis");
        LLMResponse resp = provider.generate(req);

        assertNotNull(resp);
        assertTrue(resp.getText().contains("testCases"));
    }
}
