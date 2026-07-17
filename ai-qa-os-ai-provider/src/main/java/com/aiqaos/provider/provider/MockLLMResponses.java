package com.aiqaos.provider.provider;

/**
 * Placeholder response bodies for providers that are not yet wired to a real LLM API.
 * The JSON shape satisfies every AgentType branch in LLMResponseValidator (rootCause,
 * reportId/summary, testCases, scripts all present) so the pipeline can run end-to-end
 * against these stubs before real provider integrations are added.
 */
public final class MockLLMResponses {

    private MockLLMResponses() {}

    public static String json(String providerName) {
        return "{"
            + "\"rootCause\": \"Mock response from " + providerName + " stub - no real LLM call configured.\","
            + "\"failureCategory\": \"UNKNOWN\","
            + "\"selfHealingSuggestion\": \"No self-healing recommendation generated.\","
            + "\"severity\": \"MEDIUM\","
            + "\"priority\": \"P2\","
            + "\"status\": \"OPEN\","
            + "\"confidence\": 0.5,"
            + "\"requiresRegeneration\": false,"
            + "\"reportId\": \"mock-report\","
            + "\"summary\": \"Mock summary from " + providerName + " stub response.\","
            + "\"testCases\": [],"
            + "\"scripts\": []"
            + "}";
    }
}
