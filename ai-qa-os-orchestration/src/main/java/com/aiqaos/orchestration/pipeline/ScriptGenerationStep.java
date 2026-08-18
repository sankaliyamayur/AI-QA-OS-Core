package com.aiqaos.orchestration.pipeline;

import com.aiqaos.core.engine.WorkflowStep;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.orchestration.validation.LLMResponseValidator;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class ScriptGenerationStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScriptGenerationStep.class);

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LLMResponseValidator responseValidator;

    @Override
    public String getName() {
        return "ScriptGenerationStep";
    }

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            if (context.getQaWorkflowState() == null || context.getQaWorkflowState().getGeneratedTestCaseSuite() == null) {
                throw new IllegalStateException("GeneratedTestCaseSuite is missing from state");
            }

            GeneratedTestCaseSuite tcSuite = context.getQaWorkflowState().getGeneratedTestCaseSuite();

            // 1. Fetch Agent
            Agent<AgentRequest, AgentResponse> agent = (Agent<AgentRequest, AgentResponse>) 
                agentManager.getAgentByType(AgentType.SCRIPT_GENERATOR);
            
            if (agent == null) {
                throw new IllegalStateException("Agent of type SCRIPT_GENERATOR is not registered in the system");
            }

            // 2. Prepare master suite
            GeneratedScriptSuite masterSuite = new GeneratedScriptSuite();
            masterSuite.setSuiteId(tcSuite.getSuiteId());
            masterSuite.setScripts(new java.util.ArrayList<>());

            // Process each test case individually to avoid massive token limits
            int index = 1;
            for (com.aiqaos.core.model.GeneratedTestCaseSuite.TestCase tc : tcSuite.getTestCases()) {
                System.out.println("Generating script for test case " + index++ + " of " + tcSuite.getTestCases().size());
                
                String singleTcJson = objectMapper.writeValueAsString(tc);
                AgentRequest agentReq = new AgentRequest();
                agentReq.setPrompt(singleTcJson);
                agentReq.getMetadata().setCorrelationId(context.getMetadata().getCorrelationId());

                AgentResponse agentRes = agent.execute(agentReq, new AgentContext());

                if ("FAILED".equals(agentRes.getStatus())) {
                    throw new RuntimeException("Script Generator Agent failed for TC " + tc.getId() + ": " + agentRes.getMessage());
                }
                response.setConfidence(agentRes.getConfidenceScore()); // AI-1: surface confidence to the gate

                String normalizedJson = responseValidator.validateAndNormalize(AgentType.SCRIPT_GENERATOR, agentRes.getContent());
                GeneratedScriptSuite singleSuite = objectMapper.readValue(normalizedJson, GeneratedScriptSuite.class);
                
                if (singleSuite.getScripts() != null) {
                    masterSuite.getScripts().addAll(singleSuite.getScripts());
                }
            }

            context.getQaWorkflowState().setGeneratedScriptSuite(masterSuite);

            // Producing no script is a failure, not a success.
            //
            // The agent can return a well-formed but empty suite — a weak or truncated model answer
            // normalises to scripts:[] without throwing. Reporting SUCCESS there is worse than
            // failing: ExecutionStep writes nothing, Playwright runs whatever .spec files happen to
            // be left in the tests directory from a previous run, and those stale results are
            // reported as this run's. A live run did exactly that — it "passed" two tests written six
            // hours earlier by an unrelated run while this run generated nothing at all.
            if (masterSuite.getScripts() == null || masterSuite.getScripts().isEmpty()) {
                response.setStatus("FAILED");
                response.setMessage("Failed in ScriptGenerationStep: the agent returned no usable "
                        + "script for any of the " + tcSuite.getTestCases().size() + " test case(s). "
                        + "Refusing to continue, because executing a stale tests directory would "
                        + "report a previous run's results as this one's.");
                return response;
            }

            response.setStatus("SUCCESS");
            response.setMessage("Successfully generated automation scripts ("
                    + masterSuite.getScripts().size() + ")");
        } catch (Exception e) {
            log.error("[ScriptGenerationStep] Failed: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setMessage("Failed in ScriptGenerationStep: " + e.getMessage());
        }
        return response;
    }
}
