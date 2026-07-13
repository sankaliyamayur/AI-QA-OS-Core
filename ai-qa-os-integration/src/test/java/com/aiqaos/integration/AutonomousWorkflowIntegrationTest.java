package com.aiqaos.integration;

import com.aiqaos.integration.context.IntegrationResult;
import com.aiqaos.integration.orchestrator.AutonomousWorkflowOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class AutonomousWorkflowIntegrationTest {

    @Autowired
    private AutonomousWorkflowOrchestrator orchestrator;

    @Test
    public void testAutonomousQAOrchestrationFlow() {
        IntegrationResult result = orchestrator.executeAutonomousQA(
            "Analyze login user story and automate Playwright test script executions", 
            "test-user"
        );

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus(), "Orchestration failed: " + result.getMessage());
        assertNotNull(result.getSummary());
        assertEquals("SUCCESS", result.getSummary().getStatus());
        assertFalse(result.getSummary().getAgentSummaries().isEmpty());
    }
}