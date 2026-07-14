package com.aiqaos.workflow.validation;

import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

public class LLMResponseValidatorTest {

    private LLMResponseValidator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        validator = new LLMResponseValidator();
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(validator, "objectMapper", objectMapper);
    }

    @Test
    public void testCasingNormalizationAndFieldRepairSuccess() throws Exception {
        // Raw JSON with alternative casing "testcases" and missing optional fields in nested list
        String rawJson = "{\n" +
                "  \"suiteId\": \"suite-123\",\n" +
                "  \"testcases\": [\n" +
                "    {\n" +
                "      \"id\": \"TC1\",\n" +
                "      \"name\": \"Verify Login Page\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String normalizedJson = validator.validateAndNormalize(AgentType.TEST_CASE_GENERATOR, rawJson);

        assertNotNull(normalizedJson);
        assertTrue(normalizedJson.contains("\"testCases\""));
        assertFalse(normalizedJson.contains("\"testcases\""));

        // Deserialize and check field repair
        GeneratedTestCaseSuite suite = objectMapper.readValue(normalizedJson, GeneratedTestCaseSuite.class);
        assertEquals("suite-123", suite.getSuiteId());
        assertEquals(1, suite.getTestCases().size());

        GeneratedTestCaseSuite.TestCase tc = suite.getTestCases().get(0);
        assertEquals("TC1", tc.getId());
        assertEquals("Verify Login Page", tc.getName());
        assertEquals("", tc.getDescription());
        assertEquals("", tc.getExpectedResult());
        assertEquals("MEDIUM", tc.getPriority());
        assertNotNull(tc.getSteps());
        assertTrue(tc.getSteps().isEmpty());
    }

    @Test
    public void testSuiteIdAndTestCaseArrayRepairIfMissing() throws Exception {
        // Raw JSON without suiteId and without testCases array
        String rawJson = "{}";

        String normalizedJson = validator.validateAndNormalize(AgentType.TEST_CASE_GENERATOR, rawJson);

        assertNotNull(normalizedJson);
        GeneratedTestCaseSuite suite = objectMapper.readValue(normalizedJson, GeneratedTestCaseSuite.class);
        
        assertNotNull(suite.getSuiteId());
        assertTrue(suite.getSuiteId().startsWith("suite-generated-"));
        assertNotNull(suite.getTestCases());
        assertTrue(suite.getTestCases().isEmpty());
    }

    @Test
    public void testValidationFailureOnInvalidJson() {
        String invalidJson = "{invalid-json}";
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validateAndNormalize(AgentType.TEST_CASE_GENERATOR, invalidJson);
        });
    }

    @Test
    public void testScriptGenerationCasingAndFieldRepairSuccess() throws Exception {
        String rawJson = "{\n" +
                "  \"suiteId\": \"suite-999\",\n" +
                "  \"scriptsuite\": [\n" +
                "    {\n" +
                "      \"scriptID\": \"script-1\",\n" +
                "      \"testcaseId\": \"TC-1\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String normalizedJson = validator.validateAndNormalize(AgentType.SCRIPT_GENERATOR, rawJson);

        assertNotNull(normalizedJson);
        assertTrue(normalizedJson.contains("\"scripts\""));
        assertTrue(normalizedJson.contains("\"scriptId\""));
        assertTrue(normalizedJson.contains("\"testCaseId\""));

        com.aiqaos.core.model.GeneratedScriptSuite suite = objectMapper.readValue(normalizedJson, com.aiqaos.core.model.GeneratedScriptSuite.class);
        assertEquals("suite-999", suite.getSuiteId());
        assertEquals(1, suite.getScripts().size());

        com.aiqaos.core.model.GeneratedScriptSuite.AutomationScript script = suite.getScripts().get(0);
        assertEquals("script-1", script.getScriptId());
        assertEquals("TC-1", script.getTestCaseId());
        assertEquals("Playwright", script.getFramework());
        assertEquals("JAVASCRIPT", script.getLanguage());
        assertEquals("WEB", script.getTargetPlatform());
        assertEquals("throw new UnsupportedOperationException(\"LLM did not generate automation code.\");", script.getCode());
    }

    @Test
    public void testScriptGenerationDuplicateTestCaseIdThrowsException() {
        String rawJson = "{\n" +
                "  \"scripts\": [\n" +
                "    {\"scriptId\":\"s1\", \"testCaseId\":\"TC-1\"},\n" +
                "    {\"scriptId\":\"s2\", \"testCaseId\":\"TC-1\"}\n" +
                "  ]\n" +
                "}";
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validateAndNormalize(AgentType.SCRIPT_GENERATOR, rawJson);
        });
    }

    @Test
    public void testScriptGenerationDuplicateScriptIdThrowsException() {
        String rawJson = "{\n" +
                "  \"scripts\": [\n" +
                "    {\"scriptId\":\"s1\", \"testCaseId\":\"TC-1\"},\n" +
                "    {\"scriptId\":\"s1\", \"testCaseId\":\"TC-2\"}\n" +
                "  ]\n" +
                "}";
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validateAndNormalize(AgentType.SCRIPT_GENERATOR, rawJson);
        });
    }

    @Test
    public void testScriptGenerationUnsupportedFrameworkThrowsException() {
        String rawJson = "{\n" +
                "  \"scripts\": [\n" +
                "    {\"scriptId\":\"s1\", \"testCaseId\":\"TC-1\", \"framework\":\"ABCXYZ\"}\n" +
                "  ]\n" +
                "}";
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validateAndNormalize(AgentType.SCRIPT_GENERATOR, rawJson);
        });
    }

    @Test
    public void testScriptGenerationUnsupportedLanguageThrowsException() {
        String rawJson = "{\n" +
                "  \"scripts\": [\n" +
                "    {\"scriptId\":\"s1\", \"testCaseId\":\"TC-1\", \"language\":\"GOLANG\"}\n" +
                "  ]\n" +
                "}";
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validateAndNormalize(AgentType.SCRIPT_GENERATOR, rawJson);
        });
    }

    @Test
    public void testBugAnalysisCasingAndRecommendationMappingSuccess() throws Exception {
        String rawJson = "{\n" +
                "  \"rootcause\": \"CSS selector mismatch\",\n" +
                "  \"failurecategory\": \"ELEMENT_NOT_FOUND\",\n" +
                "  \"recommendation\": \"Update selector target\"\n" +
                "}";

        String normalizedJson = validator.validateAndNormalize(AgentType.BUG_ANALYZER, rawJson);

        assertNotNull(normalizedJson);
        assertTrue(normalizedJson.contains("\"rootCause\""));
        assertTrue(normalizedJson.contains("\"failureCategory\""));
        assertTrue(normalizedJson.contains("\"selfHealingSuggestion\""));

        com.aiqaos.core.model.BugAnalysisReport report = objectMapper.readValue(normalizedJson, com.aiqaos.core.model.BugAnalysisReport.class);
        assertEquals("CSS selector mismatch", report.getRootCause());
        assertEquals("ELEMENT_NOT_FOUND", report.getFailureCategory());
        assertEquals("Update selector target", report.getSelfHealingSuggestion());
        assertEquals("HIGH", report.getSeverity());
        assertEquals("P1", report.getPriority());
        assertEquals("OPEN", report.getStatus());
        assertNotNull(report.getReportId());
    }

    @Test
    public void testBugAnalysisThrowsExceptionIfRootCauseMissing() {
        String rawJson = "{\n" +
                "  \"failureCategory\": \"ASSERTION_ERROR\"\n" +
                "}";
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validateAndNormalize(AgentType.BUG_ANALYZER, rawJson);
        });
    }

    @Test
    public void testReporterCasingNormalization() throws Exception {
        String rawJson = "{" +
                "\"reportId\":\"RPT-001\"," +
                "\"summary\":\"All tests passed\"," +
                "\"overallresult\":\"PASS\"," +
                "\"totaltests\":10," +
                "\"passedtests\":8," +
                "\"failedtests\":2," +
                "\"passpercentage\":80.0" +
                "}";

        String normalized = validator.validateAndNormalize(AgentType.REPORTER, rawJson);

        assertNotNull(normalized);
        assertTrue(normalized.contains("\"overallResult\""));
        assertFalse(normalized.contains("\"overallresult\""));
        assertTrue(normalized.contains("\"totalTestCases\""));
        assertFalse(normalized.contains("\"totaltests\""));
        assertTrue(normalized.contains("\"passedTests\""));
        assertTrue(normalized.contains("\"failedTests\""));
    }

    @Test
    public void testReporterDefaultFieldRepair() throws Exception {
        // Minimal JSON — missing status, overallResult, reportVersion, generatedBy, recommendations
        String rawJson = "{\"reportId\":\"RPT-002\",\"summary\":\"Partial run\"}";

        String normalized = validator.validateAndNormalize(AgentType.REPORTER, rawJson);

        assertTrue(normalized.contains("\"status\":\"COMPLETED\""));
        assertTrue(normalized.contains("\"overallResult\":\"UNKNOWN\""));
        assertTrue(normalized.contains("\"reportVersion\":\"v1.0\""));
        assertTrue(normalized.contains("\"generatedBy\":\"AI-QA-OS ReportingAgent\""));
        assertTrue(normalized.contains("\"recommendations\":[]"));
    }

    @Test
    public void testReporterPassPercentageCalculation() throws Exception {
        String rawJson = "{" +
                "\"reportId\":\"RPT-003\"," +
                "\"summary\":\"Execution report\"," +
                "\"totalTestCases\":10," +
                "\"passedTests\":8," +
                "\"failedTests\":2" +
                "}";

        String normalized = validator.validateAndNormalize(AgentType.REPORTER, rawJson);

        assertTrue(normalized.contains("\"passPercentage\":80.0"));
    }

    @Test
    public void testReporterRejectsWhenBothReportIdAndSummaryMissing() {
        String rawJson = "{\"overallResult\":\"FAIL\",\"totalTestCases\":5}";
        assertThrows(IllegalArgumentException.class, () -> {
            validator.validateAndNormalize(AgentType.REPORTER, rawJson);
        });
    }

    @Test
    public void testLearningEngineerCasingAndDefaultNormalization() throws Exception {
        String rawJson = "{" +
                "\"failurepatterns\":[" +
                "  {\"patternId\":\"PAT-1\",\"errorType\":\"TIMEOUT\",\"rootCause\":\"network delay\"}" +
                "]," +
                "\"selfhealingrecommendations\":[" +
                "  {\"issue\":\"locator failed\",\"suggestedAction\":\"update xpath\",\"regeneratescript\":false,\"updatelocator\":true}" +
                "]" +
                "}";

        String normalized = validator.validateAndNormalize(AgentType.LEARNING_ENGINEER, rawJson);

        assertNotNull(normalized);
        assertTrue(normalized.contains("\"patterns\""));
        assertFalse(normalized.contains("\"failurepatterns\""));
        assertTrue(normalized.contains("\"recommendations\""));
        assertFalse(normalized.contains("\"selfhealingrecommendations\""));

        assertTrue(normalized.contains("\"regenerateScript\":false"));
        assertTrue(normalized.contains("\"updateLocator\":true"));
        assertTrue(normalized.contains("\"actionType\":\"LOCATOR_UPDATE\""));
        assertTrue(normalized.contains("\"recommendationId\":\"REC-"));
    }
}

