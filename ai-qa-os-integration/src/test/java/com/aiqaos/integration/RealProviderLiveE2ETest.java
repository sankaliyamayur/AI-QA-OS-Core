package com.aiqaos.integration;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.core.model.QAAnalysisResult;
import com.aiqaos.core.requirement.RequirementContext;
import com.aiqaos.orchestration.pipeline.AutonomousQAPipelineOrchestrator;
import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.manager.LLMProviderManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {
    "aiqaos.ai.simulator.enabled=false",
    "aiqaos.ai.cache.enabled=false",
    "qa.max-tokens.qa-analysis=8192",
    "qa.max-tokens.test-case-generation=8192",
    "qa.max-tokens.script-generation=8192"
})
public class RealProviderLiveE2ETest {

    @Autowired
    private AutonomousQAPipelineOrchestrator orchestrator;

    @Autowired(required = false)
    private LLMProviderManager providerManager;

    @Autowired(required = false)
    private List<LLMProvider> registeredProviders;

    @Autowired(required = false)
    private com.aiqaos.core.repository.ModuleRepository moduleRepo;

    @Test
    @DisplayName("Run Live E2E Autonomous QA Pipeline on US-001.md with REAL Provider Mode")
    public void runLiveE2EWithRealProviders() {
        System.out.println("================================================================================");
        System.out.println("LIVE E2E RUNBOOK EXECUTION — REAL PROVIDER MODE");
        System.out.println("================================================================================");

        if (moduleRepo != null && moduleRepo.findAll().isEmpty()) {
            com.aiqaos.core.entity.ModuleEntity mod = new com.aiqaos.core.entity.ModuleEntity();
            mod.setId("admin-login");
            mod.setName("Admin Authentication Module");
            mod.setDescription("Contains test cases for the Admin portal login and authentication flows.");
            mod.setRequirementPath("resources/user-stories/Login/US-001.md");
            moduleRepo.save(mod);
        }

        if (registeredProviders != null) {
            System.out.println("Registered LLM Providers in Spring Context:");
            for (LLMProvider p : registeredProviders) {
                System.out.println(" - " + p.getProviderName() + " (" + p.getClass().getSimpleName() + ")");
            }
        }

        java.io.File storyFile = new java.io.File("../resources/user-stories/Login/US-001.md");
        if (!storyFile.exists()) {
            storyFile = new java.io.File("resources/user-stories/Login/US-001.md");
        }
        String storyPath = storyFile.getAbsolutePath();

        WorkflowRequest request = new WorkflowRequest();
        request.setWorkflowName("AUTONOMOUS_QA_PIPELINE");
        request.getInputs().put("storyPath", storyPath);

        WorkflowContext context = new WorkflowContext();
        context.getVariables().put("storyPath", storyPath);
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        System.out.println("Launching 9-step Autonomous QA Pipeline for storyPath: resources/user-stories/Login/US-001.md...");
        
        WorkflowResponse response = null;
        try {
            response = orchestrator.runPipeline(request, context);
            System.out.println("\n>>> PIPELINE RUN COMPLETED <<<");
            System.out.println("Pipeline Final Status : " + response.getStatus());
            System.out.println("Pipeline Final Message: " + response.getMessage());
        } catch (Exception e) {
            System.out.println("\n>>> PIPELINE RUN EXCEPTION <<<");
            System.out.println("Exception Type: " + e.getClass().getName());
            System.out.println("Exception Msg : " + e.getMessage());
            e.printStackTrace(System.out);
        }

        System.out.println("\n================================================================================");
        System.out.println("DETAILED STEP & ARTIFACT EVIDENCE:");
        System.out.println("================================================================================");

        AutonomousQAWorkflowState qaState = context.getQaWorkflowState();
        QAAnalysisResult analysis = qaState != null ? qaState.getQaAnalysisResult() : null;
        if (analysis != null) {
            System.out.println("\n1. QAAnalysisResult:");
            System.out.println("   Summary: " + analysis.getAnalysisSummary());
            System.out.println("   Identified Scenarios: " + analysis.getIdentifiedScenarios());
        } else {
            System.out.println("\n1. QAAnalysisResult: NULL");
        }

        GeneratedTestCaseSuite tcSuite = qaState.getGeneratedTestCaseSuite();
        if (tcSuite != null && tcSuite.getTestCases() != null) {
            System.out.println("\n2. GeneratedTestCaseSuite:");
            System.out.println("   Suite ID: " + tcSuite.getSuiteId());
            System.out.println("   Test Case Count: " + tcSuite.getTestCases().size());
            for (GeneratedTestCaseSuite.TestCase tc : tcSuite.getTestCases()) {
                System.out.println("   - [" + tc.getId() + "] " + tc.getName() + " (Priority: " + tc.getPriority() + ")");
                System.out.println("     Steps: " + tc.getSteps());
            }
        } else {
            System.out.println("\n2. GeneratedTestCaseSuite: NULL or Empty");
        }

        GeneratedScriptSuite scriptSuite = qaState.getGeneratedScriptSuite();
        if (scriptSuite != null && scriptSuite.getScripts() != null) {
            System.out.println("\n3. GeneratedScriptSuite:");
            System.out.println("   Script Count: " + scriptSuite.getScripts().size());
            for (GeneratedScriptSuite.AutomationScript script : scriptSuite.getScripts()) {
                System.out.println("   - Script ID: " + script.getScriptId() + " (TestCaseId: " + script.getTestCaseId() + ")");
                System.out.println("     Code Snippet:\n" + script.getCode());
            }
        } else {
            System.out.println("\n3. GeneratedScriptSuite: NULL or Empty");
        }

        if (qaState.getExecutionResult() != null) {
            System.out.println("\n4. ExecutionResult:");
            System.out.println("   Success: " + qaState.getExecutionResult().isSuccess());
            System.out.println("   Passed Tests: " + qaState.getExecutionResult().getPassed());
            System.out.println("   Failed Tests: " + qaState.getExecutionResult().getFailed());
            System.out.println("   Skipped Tests: " + qaState.getExecutionResult().getSkipped());
            System.out.println("   Logs:\n" + qaState.getExecutionResult().getLogs());
        } else {
            System.out.println("\n4. ExecutionResult: NULL");
        }
        System.out.println("================================================================================");
    }
}
