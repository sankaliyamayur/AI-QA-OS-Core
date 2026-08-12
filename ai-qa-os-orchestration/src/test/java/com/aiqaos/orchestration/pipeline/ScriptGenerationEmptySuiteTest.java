package com.aiqaos.orchestration.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.orchestration.validation.LLMResponseValidator;
import tools.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Generating no script must fail the step, not pass it.
 *
 * <p><b>The false green this closes.</b> The agent can return a well-formed but empty suite — a weak
 * or truncated model answer normalises to {@code scripts:[]} without throwing — and the step used to
 * report SUCCESS regardless. ExecutionStep then wrote nothing, Playwright ran whatever {@code .spec}
 * files were already sitting in the tests directory, and those results were reported as this run's.
 *
 * <p>That is not hypothetical. A live Ollama-backed run passed "2 tests" that had been written six
 * hours earlier by an unrelated run, while generating nothing itself. Every step reported SUCCESS and
 * the workflow finished 9/9.
 */
class ScriptGenerationEmptySuiteTest {

    private final AgentManager agentManager = mock(AgentManager.class);
    private final LLMResponseValidator validator = mock(LLMResponseValidator.class);

    @SuppressWarnings("unchecked")
    private ScriptGenerationStep stepReturning(String normalizedJson) throws Exception {
        Agent<AgentRequest, AgentResponse> agent = mock(Agent.class);
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setStatus("SUCCESS");
        agentResponse.setContent(normalizedJson);
        when(agent.execute(any(), any(AgentContext.class))).thenReturn(agentResponse);
        when(agentManager.getAgentByType(AgentType.SCRIPT_GENERATOR)).thenReturn((Agent) agent);
        when(validator.validateAndNormalize(any(), any())).thenReturn(normalizedJson);

        ScriptGenerationStep step = new ScriptGenerationStep();
        inject(step, "agentManager", agentManager);
        inject(step, "objectMapper", new ObjectMapper());
        inject(step, "responseValidator", validator);
        return step;
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static WorkflowContext contextWithOneTestCase() {
        GeneratedTestCaseSuite.TestCase tc = new GeneratedTestCaseSuite.TestCase();
        tc.setId("TC-001");
        tc.setName("valid login");

        GeneratedTestCaseSuite suite = new GeneratedTestCaseSuite();
        suite.setSuiteId("suite-1");
        suite.setTestCases(List.of(tc));

        AutonomousQAWorkflowState state = new AutonomousQAWorkflowState();
        state.setGeneratedTestCaseSuite(suite);

        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(state);
        return context;
    }

    @Test
    @DisplayName("an empty scripts[] must FAIL the step, never report success")
    void emptyScriptSuiteFailsTheStep() throws Exception {
        ScriptGenerationStep step = stepReturning("{\"suiteId\":\"s\",\"scripts\":[]}");

        WorkflowResponse response = step.execute(new WorkflowRequest(), contextWithOneTestCase());

        assertEquals("FAILED", response.getStatus(),
                "a run that generated no script must not continue into ExecutionStep, which would "
                        + "execute leftover .spec files from a previous run");
        assertTrue(response.getMessage().contains("no usable script"), response.getMessage());
        assertTrue(response.getMessage().contains("stale"),
                "the message should say why continuing is unsafe: " + response.getMessage());
    }

    @Test
    @DisplayName("a real script still succeeds, and the count is reported")
    void aGeneratedScriptStillSucceeds() throws Exception {
        ScriptGenerationStep step = stepReturning(
                "{\"suiteId\":\"s\",\"scripts\":[{\"scriptId\":\"sc-1\",\"testCaseId\":\"TC-001\","
                        + "\"language\":\"JAVASCRIPT\",\"framework\":\"Playwright\","
                        + "\"code\":\"import { test } from '@playwright/test';\"}]}");

        WorkflowResponse response = step.execute(new WorkflowRequest(), contextWithOneTestCase());

        assertEquals("SUCCESS", response.getStatus(), response.getMessage());
        assertTrue(response.getMessage().contains("(1)"), response.getMessage());
    }
}
