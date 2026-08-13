package com.aiqaos.orchestration.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.entity.TestCaseEntity;
import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.model.QAAnalysisResult;
import com.aiqaos.core.repository.ModuleRepository;
import com.aiqaos.core.repository.TestCaseRepository;
import com.aiqaos.orchestration.validation.LLMResponseValidator;
import tools.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TestCaseGenerationEmptySuiteTest {

    private final AgentManager agentManager = mock(AgentManager.class);
    private final LLMResponseValidator validator = mock(LLMResponseValidator.class);
    private final TestCaseRepository testCaseRepo = mock(TestCaseRepository.class);
    private final ModuleRepository moduleRepo = mock(ModuleRepository.class);

    @SuppressWarnings("unchecked")
    private TestCaseGenerationStep stepReturning(String normalizedJson) throws Exception {
        Agent<AgentRequest, AgentResponse> agent = mock(Agent.class);
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setStatus("SUCCESS");
        agentResponse.setContent(normalizedJson);
        when(agent.execute(any(), any(AgentContext.class))).thenReturn(agentResponse);
        when(agentManager.getAgentByType(AgentType.TEST_CASE_GENERATOR)).thenReturn((Agent) agent);
        when(validator.validateAndNormalize(any(), any())).thenReturn(normalizedJson);
        when(testCaseRepo.findById(any())).thenReturn(Optional.empty());

        com.aiqaos.core.entity.ModuleEntity module = new com.aiqaos.core.entity.ModuleEntity();
        module.setId("mod-1");
        module.setRequirementPath("resources/user-stories/Login/US-001.md");
        when(moduleRepo.findAll()).thenReturn(List.of(module));

        TestCaseGenerationStep step = new TestCaseGenerationStep();
        inject(step, "agentManager", agentManager);
        inject(step, "objectMapper", new ObjectMapper());
        inject(step, "responseValidator", validator);
        inject(step, "testCaseRepo", testCaseRepo);
        inject(step, "moduleRepo", moduleRepo);
        return step;
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static WorkflowContext contextWithQAAnalysisResult() {
        QAAnalysisResult analysis = new QAAnalysisResult();
        analysis.setAnalysisSummary("Test requirement");

        com.aiqaos.core.requirement.RequirementContext reqContext = new com.aiqaos.core.requirement.RequirementContext();
        reqContext.getAdditionalMetadata().put("storyPath", "resources/user-stories/Login/US-001.md");

        AutonomousQAWorkflowState state = new AutonomousQAWorkflowState();
        state.setQaAnalysisResult(analysis);
        state.setRequirementContext(reqContext);

        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(state);
        return context;
    }

    @Test
    @DisplayName("an empty testCases[] must FAIL the step, refusing to proceed")
    void emptyTestCaseListFailsTheStep() throws Exception {
        TestCaseGenerationStep step = stepReturning("{\"suiteId\":\"s\",\"testCases\":[]}");

        WorkflowResponse response = step.execute(new WorkflowRequest(), contextWithQAAnalysisResult());

        assertEquals("FAILED", response.getStatus(),
                "a run that generated no test cases must fail the step");
        assertTrue(response.getMessage().contains("returned no test cases"), response.getMessage());
    }

    @Test
    @DisplayName("a valid non-empty suite still succeeds")
    void validTestCasesStillSucceed() throws Exception {
        TestCaseGenerationStep step = stepReturning(
                "{\"suiteId\":\"s\",\"testCases\":[{\"id\":\"TC-001\",\"name\":\"Test 1\","
                        + "\"description\":\"Desc\",\"priority\":\"P1\",\"steps\":[\"step 1\"]}]}");

        WorkflowResponse response = step.execute(new WorkflowRequest(), contextWithQAAnalysisResult());

        assertEquals("SUCCESS", response.getStatus(), response.getMessage());
    }

    @Test
    @DisplayName("an empty test suite must NOT delete existing test cases in database")
    void emptySuiteDoesNotDeleteThePreviousSuite() throws Exception {
        TestCaseGenerationStep step = stepReturning("{\"suiteId\":\"s\",\"testCases\":[]}");
        TestCaseEntity existingTc = new TestCaseEntity();
        existingTc.setId("TC-OLD-001");
        when(testCaseRepo.findByModuleId(any())).thenReturn(List.of(existingTc));

        step.execute(new WorkflowRequest(), contextWithQAAnalysisResult());

        verify(testCaseRepo, never()).deleteAll(any());
    }
}
