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

    @Test
    @DisplayName("DX-3 regression (live E2E): each pipeline purpose routes to schema-valid JSON, even when the "
            + "prompt mentions Playwright — previously TEST_CASE/SCRIPT matched keyword branches and returned non-JSON")
    void testPurposeRoutingReturnsSchemaValidJsonPerStep() throws Exception {
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

        // The real test-case prompt for US-001 mentions Playwright/Selenium/automation, which used to
        // match the "script" branch and return a //-commented script the step could not parse as JSON.
        String tc = provider.generate(new LLMRequest(
                "Generate test cases. Suitable for Playwright, Selenium, automation.",
                "local-simulator", "TEST_CASE_GENERATION")).getText();
        assertTrue(mapper.readTree(tc).has("testCases"), "TEST_CASE_GENERATION must be JSON with testCases");

        // SCRIPT_GENERATION must be JSON with a non-empty scripts[] (the ScriptGenerator schema), not raw JS.
        String script = provider.generate(new LLMRequest(
                "Produce the automation for the login requirement story",
                "local-simulator", "SCRIPT_GENERATION")).getText();
        assertFalse(script.trim().startsWith("//"), "SCRIPT_GENERATION must not be a //-commented raw script");
        assertTrue(mapper.readTree(script).get("scripts").size() > 0, "SCRIPT_GENERATION must have scripts[]");

        // BUG_ANALYSIS requires a non-empty rootCause.
        String bug = provider.generate(new LLMRequest("analyze the failure", "local-simulator", "BUG_ANALYSIS")).getText();
        assertFalse(mapper.readTree(bug).get("rootCause").asText().isBlank(), "BUG_ANALYSIS needs rootCause");

        // REPORT_GENERATION requires reportId or summary.
        String report = provider.generate(new LLMRequest("summarize the run", "local-simulator", "REPORT_GENERATION")).getText();
        assertTrue(mapper.readTree(report).has("summary"), "REPORT_GENERATION needs summary");

        // SELF_HEALING requires a non-empty healingAction (NOT the legacy \"healed\" field).
        String heal = provider.generate(new LLMRequest("decide healing", "local-simulator", "SELF_HEALING")).getText();
        assertFalse(mapper.readTree(heal).get("healingAction").asText().isBlank(), "SELF_HEALING needs healingAction");

        // LEARNING_ENGINE just needs a JSON object.
        String learn = provider.generate(new LLMRequest("learn", "local-simulator", "LEARNING_ENGINE")).getText();
        assertTrue(mapper.readTree(learn).isObject(), "LEARNING_ENGINE must be a JSON object");
    }
}
