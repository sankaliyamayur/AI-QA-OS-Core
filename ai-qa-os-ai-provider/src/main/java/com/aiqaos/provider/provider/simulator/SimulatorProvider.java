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

        log.info("DX-3: SimulatorProvider processing request promptLength={} model={}", prompt.length(), model);

        String generatedText = generateSimulatedResponse(prompt);
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

    private String generateSimulatedResponse(String prompt) {
        String lower = prompt.toLowerCase();

        if (lower.contains("heal") || lower.contains("repair") || lower.contains("broken")) {
            return """
                {
                  "healed": true,
                  "originalSelector": "button#submit-old",
                  "repairedSelector": "button[type='submit']",
                  "confidence": 0.95,
                  "strategy": "DOM_SIMILARITY"
                }
                """;
        }

        if (lower.contains("locator") || lower.contains("element") || lower.contains("xpath")) {
            return """
                {
                  "targetLocators": [
                    { "name": "usernameInput", "selector": "#username", "strategy": "CSS" },
                    { "name": "passwordInput", "selector": "#password", "strategy": "CSS" },
                    { "name": "submitButton", "selector": "button[type='submit']", "strategy": "CSS" }
                  ],
                  "confidenceScore": 0.98
                }
                """;
        }

        if (lower.contains("script") || lower.contains("playwright") || lower.contains("code")) {
            return """
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
        }

        if (lower.contains("requirement") || lower.contains("story") || lower.contains("prd")) {
            return """
                {
                  "summary": "User Authentication & Dashboard Navigation",
                  "testCases": [
                    { "id": "TC-001", "title": "Valid login navigation", "priority": "HIGH" },
                    { "id": "TC-002", "title": "Invalid password validation error", "priority": "MEDIUM" }
                  ],
                  "passProbability": 0.96
                }
                """;
        }

        // Generic fallback JSON response
        return """
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
    }
}
