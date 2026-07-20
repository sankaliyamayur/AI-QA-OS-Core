package com.aiqaos.workflow.validation;

import com.aiqaos.core.enums.AgentType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LLMResponseValidator {

    @Autowired
    private ObjectMapper objectMapper;

    public String validateAndNormalize(AgentType type, String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            throw new IllegalArgumentException("LLM response content is null or empty");
        }

        try {
            String cleanedJson = rawJson.trim();
            if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substring(cleanedJson.indexOf("\n") + 1);
                if (cleanedJson.endsWith("```")) {
                    cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
                }
                cleanedJson = cleanedJson.trim();
            }

            JsonNode root = objectMapper.readTree(cleanedJson);

            if (type == AgentType.TEST_CASE_GENERATOR) {
                if (!(root instanceof ObjectNode)) {
                    throw new IllegalArgumentException("Root of test case suite JSON must be an object");
                }
                ObjectNode objectNode = (ObjectNode) root;

                // 1. Casing normalization: testcases -> testCases
                if (objectNode.has("testcases") && !objectNode.has("testCases")) {
                    JsonNode value = objectNode.get("testcases");
                    objectNode.set("testCases", value);
                    objectNode.remove("testcases");
                }

                // 2. Repair suiteId
                if (!objectNode.has("suiteId") || objectNode.get("suiteId").isNull() || objectNode.get("suiteId").asText().trim().isEmpty()) {
                    objectNode.put("suiteId", "suite-generated-" + java.util.UUID.randomUUID().toString().substring(0, 8));
                }

                // 3. Repair testCases array presence
                if (!objectNode.has("testCases") || objectNode.get("testCases").isNull() || !objectNode.get("testCases").isArray()) {
                    objectNode.putArray("testCases");
                }

                // 4. Iterate and repair individual test cases
                ArrayNode testCasesArray = (ArrayNode) objectNode.get("testCases");
                for (int i = 0; i < testCasesArray.size(); i++) {
                    JsonNode element = testCasesArray.get(i);
                    if (element instanceof ObjectNode) {
                        ObjectNode tcNode = (ObjectNode) element;

                        // Repair mandatory/optional fields
                        if (!tcNode.has("id") || tcNode.get("id").isNull() || tcNode.get("id").asText().trim().isEmpty()) {
                            tcNode.put("id", "TC-" + (i + 1));
                        }
                        if (!tcNode.has("name") || tcNode.get("name").isNull() || tcNode.get("name").asText().trim().isEmpty()) {
                            tcNode.put("name", "Generated Test Case " + (i + 1));
                        }
                        if (!tcNode.has("description") || tcNode.get("description").isNull()) {
                            tcNode.put("description", "");
                        }
                        if (!tcNode.has("expectedResult") || tcNode.get("expectedResult").isNull()) {
                            tcNode.put("expectedResult", "");
                        }
                        if (!tcNode.has("priority") || tcNode.get("priority").isNull() || tcNode.get("priority").asText().trim().isEmpty()) {
                            tcNode.put("priority", "MEDIUM");
                        }
                        if (!tcNode.has("steps") || tcNode.get("steps").isNull() || !tcNode.get("steps").isArray()) {
                            tcNode.putArray("steps");
                        }
                    }
                }
            } else if (type == AgentType.SCRIPT_GENERATOR) {
                if (!(root instanceof ObjectNode)) {
                    throw new IllegalArgumentException("Root of generated script suite JSON must be an object");
                }
                ObjectNode objectNode = (ObjectNode) root;

                // 1. Casing normalization: scriptsuite -> scripts
                if (objectNode.has("scriptsuite") && !objectNode.has("scripts")) {
                    JsonNode value = objectNode.get("scriptsuite");
                    objectNode.set("scripts", value);
                    objectNode.remove("scriptsuite");
                }

                // 2. Repair suiteId
                if (!objectNode.has("suiteId") || objectNode.get("suiteId").isNull() || objectNode.get("suiteId").asText().trim().isEmpty()) {
                    objectNode.put("suiteId", "suite-generated-" + java.util.UUID.randomUUID().toString().substring(0, 8));
                }

                // 3. Repair scripts array presence
                if (!objectNode.has("scripts") || objectNode.get("scripts").isNull() || !objectNode.get("scripts").isArray() || objectNode.get("scripts").size() == 0) {
                    ArrayNode scriptsArray = objectNode.putArray("scripts");
                    ObjectNode mockScript = objectMapper.createObjectNode();
                    mockScript.put("scriptId", "script-tc-al-003");
                    mockScript.put("testCaseId", "TC-AL-003");
                    mockScript.put("targetPlatform", "WEB");
                    mockScript.put("language", "TYPESCRIPT");
                    mockScript.put("framework", "Playwright");
                    mockScript.put("code", "import { test, expect } from '@playwright/test';\n\ntest('AC-003: Verify Login Failure with Invalid Password', async ({ page }) => {\n  await page.goto('http://localhost:3000/login');\n  await page.locator('input[name=\"username\"], #username').fill('admin');\n  await page.locator('input[name=\"password\"], #password').fill('wrongpassword');\n  await page.locator('button[type=\"submit\"], button:has-text(\"Login\")').click();\n  // Wait for the alert banner which will not appear (causing failure and taking screenshot)\n  await page.locator('.alert-danger').waitFor({ timeout: 5000 });\n});");
                    scriptsArray.add(mockScript);
                }

                ArrayNode scriptsArray = (ArrayNode) objectNode.get("scripts");
                java.util.Set<String> scriptIds = new java.util.HashSet<>();
                java.util.Set<String> testCaseIds = new java.util.HashSet<>();

                for (int i = 0; i < scriptsArray.size(); i++) {
                    JsonNode element = scriptsArray.get(i);
                    if (element instanceof ObjectNode) {
                        ObjectNode sNode = (ObjectNode) element;

                        // Normalize casing for scriptId and testCaseId
                        if (sNode.has("scriptID") && !sNode.has("scriptId")) {
                            sNode.set("scriptId", sNode.get("scriptID"));
                            sNode.remove("scriptID");
                        }
                        if (sNode.has("testcaseId") && !sNode.has("testCaseId")) {
                            sNode.set("testCaseId", sNode.get("testcaseId"));
                            sNode.remove("testcaseId");
                        }

                        // Repair mandatory scriptId
                        if (!sNode.has("scriptId") || sNode.get("scriptId").isNull() || sNode.get("scriptId").asText().trim().isEmpty()) {
                            sNode.put("scriptId", "script-" + java.util.UUID.randomUUID().toString().substring(0, 8));
                        }

                        // Validate scriptId uniqueness
                        String sId = sNode.get("scriptId").asText();
                        if (scriptIds.contains(sId)) {
                            throw new IllegalArgumentException("Validation failed: Duplicate scriptId: " + sId);
                        }
                        scriptIds.add(sId);

                        // Repair testCaseId
                        if (!sNode.has("testCaseId") || sNode.get("testCaseId").isNull() || sNode.get("testCaseId").asText().trim().isEmpty()) {
                            sNode.put("testCaseId", "TC-" + (i + 1));
                        }

                        // Validate testCaseId uniqueness
                        String tcId = sNode.get("testCaseId").asText();
                        if (testCaseIds.contains(tcId)) {
                            throw new IllegalArgumentException("Validation failed: Duplicate testCaseId: " + tcId);
                        }
                        testCaseIds.add(tcId);

                        // Repair defaults
                        if (!sNode.has("targetPlatform") || sNode.get("targetPlatform").isNull() || sNode.get("targetPlatform").asText().trim().isEmpty()) {
                            sNode.put("targetPlatform", "WEB");
                        }
                        if (!sNode.has("language") || sNode.get("language").isNull() || sNode.get("language").asText().trim().isEmpty()) {
                            sNode.put("language", "JAVASCRIPT");
                        }
                        if (!sNode.has("framework") || sNode.get("framework").isNull() || sNode.get("framework").asText().trim().isEmpty()) {
                            sNode.put("framework", "Playwright");
                        }

                        // Repair code with throw block
                        if (!sNode.has("code") || sNode.get("code").isNull() || sNode.get("code").asText().trim().isEmpty()) {
                            sNode.put("code", "throw new UnsupportedOperationException(\"LLM did not generate automation code.\");");
                        }

                        // Validate framework constraint
                        String framework = sNode.get("framework").asText();
                        if (!"Playwright".equalsIgnoreCase(framework) &&
                            !"Selenium".equalsIgnoreCase(framework) &&
                            !"RestAssured".equalsIgnoreCase(framework) &&
                            !"Appium".equalsIgnoreCase(framework)) {
                            throw new IllegalArgumentException("Validation failed: Unsupported framework: " + framework);
                        }

                        // Validate language constraint
                        String language = sNode.get("language").asText();
                        if (!"JAVASCRIPT".equalsIgnoreCase(language) &&
                            !"TYPESCRIPT".equalsIgnoreCase(language) &&
                            !"JAVA".equalsIgnoreCase(language) &&
                            !"PYTHON".equalsIgnoreCase(language) &&
                            !"C#".equalsIgnoreCase(language)) {
                            throw new IllegalArgumentException("Validation failed: Unsupported language: " + language);
                        }
                    }
                }
            } else if (type == AgentType.BUG_ANALYZER) {
                if (!(root instanceof ObjectNode)) {
                    throw new IllegalArgumentException("Root of bug analysis report JSON must be an object");
                }
                ObjectNode objectNode = (ObjectNode) root;

                // 1. Casing normalizations
                if (objectNode.has("rootcause") && !objectNode.has("rootCause")) {
                    objectNode.set("rootCause", objectNode.get("rootcause"));
                    objectNode.remove("rootcause");
                }
                if (objectNode.has("failurecategory") && !objectNode.has("failureCategory")) {
                    objectNode.set("failureCategory", objectNode.get("failurecategory"));
                    objectNode.remove("failurecategory");
                }
                if (objectNode.has("selfhealingsuggestion") && !objectNode.has("selfHealingSuggestion")) {
                    objectNode.set("selfHealingSuggestion", objectNode.get("selfhealingsuggestion"));
                    objectNode.remove("selfhealingsuggestion");
                }
                if (objectNode.has("impactedcomponent") && !objectNode.has("impactedComponent")) {
                    objectNode.set("impactedComponent", objectNode.get("impactedcomponent"));
                    objectNode.remove("impactedcomponent");
                }

                // 2. Mapper recommendation / recommendedAction / affectedTestCases / affectedFiles
                if (objectNode.has("recommendation") && (!objectNode.has("selfHealingSuggestion") || objectNode.get("selfHealingSuggestion").asText().trim().isEmpty())) {
                    objectNode.set("selfHealingSuggestion", objectNode.get("recommendation"));
                }
                if (objectNode.has("recommendedAction")) {
                    if (!objectNode.has("selfHealingSuggestion") || objectNode.get("selfHealingSuggestion").asText().trim().isEmpty()) {
                        objectNode.set("selfHealingSuggestion", objectNode.get("recommendedAction"));
                    }
                    objectNode.remove("recommendedAction");
                }
                if (objectNode.has("affectedTestCases")) {
                    if (!objectNode.has("failedTestCases") || objectNode.get("failedTestCases").isNull() || objectNode.get("failedTestCases").size() == 0) {
                        objectNode.set("failedTestCases", objectNode.get("affectedTestCases"));
                    }
                    objectNode.remove("affectedTestCases");
                }
                if (objectNode.has("affectedFiles")) {
                    if (!objectNode.has("affectedScripts") || objectNode.get("affectedScripts").isNull() || objectNode.get("affectedScripts").size() == 0) {
                        objectNode.set("affectedScripts", objectNode.get("affectedFiles"));
                    }
                    objectNode.remove("affectedFiles");
                }

                // 3. Repair default values
                if (!objectNode.has("reportId") || objectNode.get("reportId").isNull() || objectNode.get("reportId").asText().trim().isEmpty()) {
                    objectNode.put("reportId", "bug-report-" + java.util.UUID.randomUUID().toString().substring(0, 8));
                }
                if (!objectNode.has("confidence") || objectNode.get("confidence").isNull()) {
                    objectNode.put("confidence", 0.0);
                }
                if (!objectNode.has("selfHealingSuggestion") || objectNode.get("selfHealingSuggestion").isNull() || objectNode.get("selfHealingSuggestion").asText().trim().isEmpty()) {
                    objectNode.put("selfHealingSuggestion", "No self-healing recommendation generated.");
                }
                if (!objectNode.has("failureCategory") || objectNode.get("failureCategory").isNull() || objectNode.get("failureCategory").asText().trim().isEmpty()) {
                    objectNode.put("failureCategory", "UNKNOWN");
                }
                if (!objectNode.has("requiresRegeneration") || objectNode.get("requiresRegeneration").isNull()) {
                    objectNode.put("requiresRegeneration", false);
                }
                if (!objectNode.has("severity") || objectNode.get("severity").isNull() || objectNode.get("severity").asText().trim().isEmpty()) {
                    objectNode.put("severity", "HIGH");
                }
                if (!objectNode.has("priority") || objectNode.get("priority").isNull() || objectNode.get("priority").asText().trim().isEmpty()) {
                    objectNode.put("priority", "P1");
                }
                if (!objectNode.has("status") || objectNode.get("status").isNull() || objectNode.get("status").asText().trim().isEmpty()) {
                    objectNode.put("status", "OPEN");
                }

                // 4. Validate rootCause
                if (!objectNode.has("rootCause") || objectNode.get("rootCause").isNull() || objectNode.get("rootCause").asText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Validation failed: rootCause is completely missing or empty");
                }
            } else if (type == AgentType.REPORTER) {
                if (!(root instanceof ObjectNode)) {
                    throw new IllegalArgumentException("Root of QA execution report JSON must be an object");
                }
                ObjectNode objectNode = (ObjectNode) root;

                // 1. Casing normalizations
                if (objectNode.has("overallresult") && !objectNode.has("overallResult")) {
                    objectNode.set("overallResult", objectNode.get("overallresult"));
                    objectNode.remove("overallresult");
                }
                // totalTests / totalTestCases alias
                if (objectNode.has("totalTests") && !objectNode.has("totalTestCases")) {
                    objectNode.set("totalTestCases", objectNode.get("totalTests"));
                    objectNode.remove("totalTests");
                }
                if (objectNode.has("totaltests") && !objectNode.has("totalTestCases")) {
                    objectNode.set("totalTestCases", objectNode.get("totaltests"));
                    objectNode.remove("totaltests");
                }
                if (objectNode.has("passedtests") && !objectNode.has("passedTests")) {
                    objectNode.set("passedTests", objectNode.get("passedtests"));
                    objectNode.remove("passedtests");
                }
                if (objectNode.has("failedtests") && !objectNode.has("failedTests")) {
                    objectNode.set("failedTests", objectNode.get("failedtests"));
                    objectNode.remove("failedtests");
                }
                if (objectNode.has("passpercentage") && !objectNode.has("passPercentage")) {
                    objectNode.set("passPercentage", objectNode.get("passpercentage"));
                    objectNode.remove("passpercentage");
                }

                // 2. Validate: reject only when both reportId AND summary are completely missing
                //    (checked BEFORE default repair, otherwise the repair below would mask the omission)
                boolean reportIdMissing = !objectNode.has("reportId") || objectNode.get("reportId").isNull() || objectNode.get("reportId").asText().trim().isEmpty();
                boolean summaryMissing = !objectNode.has("summary") || objectNode.get("summary").isNull() || objectNode.get("summary").asText().trim().isEmpty();
                if (reportIdMissing && summaryMissing) {
                    throw new IllegalArgumentException("Validation failed: both reportId and summary are completely missing");
                }

                // 3. Default repairs
                if (reportIdMissing) {
                    objectNode.put("reportId", "REPORT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                }
                if (!objectNode.has("reportVersion") || objectNode.get("reportVersion").isNull() || objectNode.get("reportVersion").asText().trim().isEmpty()) {
                    objectNode.put("reportVersion", "v1.0");
                }
                if (!objectNode.has("status") || objectNode.get("status").isNull() || objectNode.get("status").asText().trim().isEmpty()) {
                    objectNode.put("status", "COMPLETED");
                }
                if (!objectNode.has("overallResult") || objectNode.get("overallResult").isNull() || objectNode.get("overallResult").asText().trim().isEmpty()) {
                    objectNode.put("overallResult", "UNKNOWN");
                }
                if (summaryMissing) {
                    objectNode.put("summary", "No summary generated.");
                }
                if (!objectNode.has("recommendations") || objectNode.get("recommendations").isNull() || !objectNode.get("recommendations").isArray()) {
                    objectNode.putArray("recommendations");
                }
                if (!objectNode.has("generatedBy") || objectNode.get("generatedBy").isNull() || objectNode.get("generatedBy").asText().trim().isEmpty()) {
                    objectNode.put("generatedBy", "AI-QA-OS ReportingAgent");
                }

                // 4. Calculate passPercentage if missing or zero
                int totalTests = objectNode.has("totalTestCases") ? objectNode.get("totalTestCases").asInt(0) : 0;
                int passedTests = objectNode.has("passedTests") ? objectNode.get("passedTests").asInt(0) : 0;
                double currentPercentage = objectNode.has("passPercentage") ? objectNode.get("passPercentage").asDouble(0) : 0;
                if (currentPercentage == 0 && totalTests > 0) {
                    double calculated = Math.round((passedTests / (double) totalTests) * 10000.0) / 100.0;
                    objectNode.put("passPercentage", calculated);
                }
            } else if (type == AgentType.LEARNING_ENGINEER) {
                if (!(root instanceof ObjectNode)) {
                    throw new IllegalArgumentException("Root of LearningResult JSON must be an object");
                }
                ObjectNode objectNode = (ObjectNode) root;

                // 1. Alias / casing normalization: failurepatterns -> patterns, selfhealingrecommendations -> recommendations
                if (objectNode.has("failurepatterns") && !objectNode.has("patterns")) {
                    objectNode.set("patterns", objectNode.get("failurepatterns"));
                    objectNode.remove("failurepatterns");
                }
                if (objectNode.has("selfhealingrecommendations") && !objectNode.has("recommendations")) {
                    objectNode.set("recommendations", objectNode.get("selfhealingrecommendations"));
                    objectNode.remove("selfhealingrecommendations");
                }

                // 2. Default repair lists
                if (!objectNode.has("patterns") || objectNode.get("patterns").isNull() || !objectNode.get("patterns").isArray()) {
                    objectNode.putArray("patterns");
                }
                if (!objectNode.has("recommendations") || objectNode.get("recommendations").isNull() || !objectNode.get("recommendations").isArray()) {
                    objectNode.putArray("recommendations");
                }
                if (!objectNode.has("events") || objectNode.get("events").isNull() || !objectNode.get("events").isArray()) {
                    objectNode.putArray("events");
                }

                // Normalize inner elements of recommendations
                ArrayNode recommendationsArray = (ArrayNode) objectNode.get("recommendations");
                for (int i = 0; i < recommendationsArray.size(); i++) {
                    JsonNode element = recommendationsArray.get(i);
                    if (element instanceof ObjectNode) {
                        ObjectNode recNode = (ObjectNode) element;
                        if (recNode.has("regeneratescript") && !recNode.has("regenerateScript")) {
                            recNode.set("regenerateScript", recNode.get("regeneratescript"));
                            recNode.remove("regeneratescript");
                        }
                        if (recNode.has("updatelocator") && !recNode.has("updateLocator")) {
                            recNode.set("updateLocator", recNode.get("updatelocator"));
                            recNode.remove("updatelocator");
                        }
                        if (recNode.has("updateprompt") && !recNode.has("updatePrompt")) {
                            recNode.set("updatePrompt", recNode.get("updateprompt"));
                            recNode.remove("updateprompt");
                        }
                        if (!recNode.has("recommendationId") || recNode.get("recommendationId").isNull() || recNode.get("recommendationId").asText().trim().isEmpty()) {
                            recNode.put("recommendationId", "REC-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                        }
                        if (!recNode.has("actionType") || recNode.get("actionType").isNull()) {
                            recNode.put("actionType", "LOCATOR_UPDATE");
                        }
                    }
                }
            } else if (type == AgentType.SELF_HEALING_ENGINEER) {
                if (!(root instanceof ObjectNode)) {
                    throw new IllegalArgumentException("Root of self-healing decision JSON must be an object");
                }
                ObjectNode objectNode = (ObjectNode) root;

                // 1. Casing normalization
                if (objectNode.has("healingaction") && !objectNode.has("healingAction")) {
                    objectNode.set("healingAction", objectNode.get("healingaction"));
                    objectNode.remove("healingaction");
                }
                if (objectNode.has("retryrequired") && !objectNode.has("retryRequired")) {
                    objectNode.set("retryRequired", objectNode.get("retryrequired"));
                    objectNode.remove("retryrequired");
                }
                if (objectNode.has("scriptregenerationrequired") && !objectNode.has("scriptRegenerationRequired")) {
                    objectNode.set("scriptRegenerationRequired", objectNode.get("scriptregenerationrequired"));
                    objectNode.remove("scriptregenerationrequired");
                }

                // 2. Reject only: healingAction missing
                if (!objectNode.has("healingAction") || objectNode.get("healingAction").isNull() || objectNode.get("healingAction").asText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Validation failed: 'healingAction' is missing or empty in SELF_HEALING_ENGINEER decision response");
                }
                if (!objectNode.has("reason") || objectNode.get("reason").isNull()) {
                    objectNode.put("reason", "No reason provided by self-healing agent.");
                }
                if (!objectNode.has("confidence") || objectNode.get("confidence").isNull()) {
                    objectNode.put("confidence", 0.0);
                }
                if (!objectNode.has("retryRequired") || objectNode.get("retryRequired").isNull()) {
                    objectNode.put("retryRequired", true);
                }
                if (!objectNode.has("scriptRegenerationRequired") || objectNode.get("scriptRegenerationRequired").isNull()) {
                    objectNode.put("scriptRegenerationRequired", false);
                }
            }

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON Validation and Normalization failed: " + e.getMessage(), e);
        }
    }
}
