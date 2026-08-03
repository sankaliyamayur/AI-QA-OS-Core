package com.aiqaos.provider.provider.simulator;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.ProviderCapability;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * DX-3: Development Sandbox & Local AI Simulator.
 *
 * Zero-API-cost, instant-response local LLM simulator for offline dev & testing.
 * Enabled via property: aiqaos.ai.simulator.enabled=true
 * Or selected via model name: "simulator", "local-simulator", "mock-gpt4"
 */
@Component("simulatorProvider")
@ConditionalOnProperty(name = "aiqaos.ai.simulator.enabled", havingValue = "true", matchIfMissing = true)
public class SimulatorProvider implements LLMProvider {

    private static final Logger log = LoggerFactory.getLogger(SimulatorProvider.class);

    private static final Set<ProviderCapability> CAPABILITIES = Set.of(
            ProviderCapability.CHAT,
            ProviderCapability.CODE_GENERATION,
            ProviderCapability.STREAMING,
            ProviderCapability.FUNCTION_CALLING
    );

    @Override
    public LLMResponse generate(LLMRequest request) {
        long startTime = System.currentTimeMillis();
        String prompt = request != null && request.getPrompt() != null ? request.getPrompt() : "";
        String model = request != null && request.getModel() != null ? request.getModel() : "local-simulator";
        String purpose = request != null ? request.getPurpose() : null;

        log.info("DX-3: SimulatorProvider processing request promptLength={} model={} purpose={}", prompt.length(), model, purpose);

        String generatedText = generateSimulatedResponse(prompt, purpose);
        long latency = Math.max(1, System.currentTimeMillis() - startTime);

        // 0-cost token usage reporting
        TokenUsage usage = new TokenUsage(prompt.length() / 4, generatedText.length() / 4);

        return new LLMResponse(generatedText, "local-simulator-v1", usage, latency);
    }

    @Override
    public String getProviderName() {
        return "simulator";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean supports(ProviderCapability capability) {
        return CAPABILITIES.contains(capability);
    }

    // ─── Intelligent Mocking Router ──────────────────────────────────────────

    private static final String HEALED_JSON = """
                {
                  "healed": true,
                  "originalSelector": "button#submit-old",
                  "repairedSelector": "button[type='submit']",
                  "confidence": 0.95,
                  "strategy": "DOM_SIMILARITY"
                }
                """;

    private static final String LOCATORS_JSON = """
                {
                  "targetLocators": [
                    { "name": "usernameInput", "selector": "#username", "strategy": "CSS" },
                    { "name": "passwordInput", "selector": "#password", "strategy": "CSS" },
                    { "name": "submitButton", "selector": "button[type='submit']", "strategy": "CSS" }
                  ],
                  "confidenceScore": 0.98
                }
                """;

    private static final String PLAYWRIGHT_SCRIPT = """
                // Auto-generated Playwright Test Script (Local AI Simulator)
                import { test, expect } from '@playwright/test';

                test('Autonomous QA Verified User Flow', async ({ page }) => {
                  await page.goto('http://localhost:3000');
                  await page.fill('#username', 'admin');
                  await page.fill('#password', 'secret');
                  await page.click('button[type="submit"]');
                  await expect(page.locator('.dashboard')).toBeVisible();
                });
                """;

    private static final String TEST_CASES_JSON = """
                {
                  "summary": "User Authentication & Dashboard Navigation",
                  "testCases": [
                    { "id": "TC-001", "title": "Valid login navigation", "priority": "HIGH" },
                    { "id": "TC-002", "title": "Invalid password validation error", "priority": "MEDIUM" }
                  ],
                  "passProbability": 0.96
                }
                """;

    private static final String GENERIC_JSON = """
            {
              "status": "SUCCESS",
              "provider": "LocalAiSimulatorProvider",
              "message": "Simulated AI reasoning response generated successfully.",
              "data": {
                "verified": true,
                "confidenceScore": 0.95
              }
            }
            """;

    // DX-3: schema-valid payloads matching LLMResponseValidator's per-agent contracts, so the
    // full autonomous pipeline can run end-to-end keyless. Each is the shape that agent's step
    // validates and normalizes (SCRIPT_GENERATOR needs a non-empty scripts[]; BUG_ANALYZER needs
    // rootCause; SELF_HEALING_ENGINEER needs healingAction; REPORTER needs reportId/summary).
    private static final String SCRIPTS_JSON = """
                {
                  "suiteId": "sim-script-suite-001",
                  "scripts": [
                    {
                      "scriptId": "script-001",
                      "testCaseId": "TC-001",
                      "targetPlatform": "WEB",
                      "language": "JAVASCRIPT",
                      "framework": "Playwright",
                      "code": "import { test, expect } from '@playwright/test'; test('valid login navigation', async ({ page }) => { await page.goto('http://localhost:3000'); await page.fill('#username', 'admin'); await page.fill('#password', 'secret'); await page.click('button[type=submit]'); await expect(page.locator('.dashboard')).toBeVisible(); });"
                    },
                    {
                      "scriptId": "script-002",
                      "testCaseId": "TC-002",
                      "targetPlatform": "WEB",
                      "language": "JAVASCRIPT",
                      "framework": "Playwright",
                      "code": "import { test, expect } from '@playwright/test'; test('invalid password shows error', async ({ page }) => { await page.goto('http://localhost:3000'); await page.fill('#username', 'admin'); await page.fill('#password', 'wrong'); await page.click('button[type=submit]'); await expect(page.locator('.error')).toBeVisible(); });"
                    }
                  ]
                }
                """;

    private static final String BUG_ANALYSIS_JSON = """
                {
                  "reportId": "bug-sim-001",
                  "rootCause": "Simulated analysis: the login submit selector was not found within the timeout.",
                  "failureCategory": "LOCATOR",
                  "impactedComponent": "LoginForm",
                  "severity": "HIGH",
                  "priority": "P2",
                  "confidence": 0.9,
                  "selfHealingSuggestion": "Update the selector to button[type='submit'].",
                  "requiresRegeneration": false,
                  "status": "OPEN"
                }
                """;

    private static final String REPORT_JSON = """
                {
                  "reportId": "REPORT-SIM-001",
                  "reportVersion": "v1.0",
                  "status": "COMPLETED",
                  "summary": "Simulated QA execution report for the admin login story.",
                  "overallResult": "PASS",
                  "totalTestCases": 2,
                  "passedTests": 2,
                  "failedTests": 0,
                  "passPercentage": 100.0,
                  "recommendations": [],
                  "generatedBy": "AI-QA-OS SimulatorProvider"
                }
                """;

    private static final String LEARNING_JSON = """
                {
                  "patterns": [],
                  "recommendations": [],
                  "events": []
                }
                """;

    private static final String SELF_HEALING_DECISION_JSON = """
                {
                  "healingAction": "RETRY",
                  "reason": "Simulated self-healing: treat as a transient failure and retry once.",
                  "confidence": 0.85,
                  "retryRequired": true,
                  "scriptRegenerationRequired": false
                }
                """;

    private String generateSimulatedResponse(String prompt) {
        return generateSimulatedResponse(prompt, null);
    }

    private String generateSimulatedResponse(String prompt, String purpose) {
        // DX-3: route deterministically by the per-step purpose the pipeline sets on each request.
        // The prompt-keyword heuristics below are only a fallback for callers that set no purpose —
        // they are unreliable on their own (e.g. a TEST_CASE_GENERATION prompt legitimately mentions
        // "Playwright", which would match the SCRIPT branch and return non-JSON the step can't parse).
        if (purpose != null) {
            switch (purpose) {
                case "TEST_CASE_GENERATION": return TEST_CASES_JSON;
                case "SCRIPT_GENERATION":    return SCRIPTS_JSON;
                case "BUG_ANALYSIS":         return BUG_ANALYSIS_JSON;
                case "REPORT_GENERATION":    return REPORT_JSON;
                case "LEARNING_ENGINE":      return LEARNING_JSON;
                case "SELF_HEALING":         return SELF_HEALING_DECISION_JSON;
                default: break; // QA_ANALYSIS / EXECUTION → any valid JSON via heuristics/fallback
            }
        }

        String lower = prompt.toLowerCase();

        if (lower.contains("heal") || lower.contains("repair") || lower.contains("broken")) {
            return HEALED_JSON;
        }

        if (lower.contains("locator") || lower.contains("element") || lower.contains("xpath")) {
            return LOCATORS_JSON;
        }

        if (lower.contains("script") || lower.contains("playwright") || lower.contains("code")) {
            return PLAYWRIGHT_SCRIPT;
        }

        if (lower.contains("requirement") || lower.contains("story") || lower.contains("prd")) {
            return TEST_CASES_JSON;
        }

        // Generic fallback JSON response
        return GENERIC_JSON;
    }
}
