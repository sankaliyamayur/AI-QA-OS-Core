package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.engine.WorkflowStep;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.core.model.QAAnalysisResult;
import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.workflow.validation.LLMResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class TestCaseGenerationStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LLMResponseValidator responseValidator;

    @Override
    public String getName() {
        return "TestCaseGenerationStep";
    }

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            if (context.getQaWorkflowState() == null || context.getQaWorkflowState().getQaAnalysisResult() == null) {
                throw new IllegalStateException("QAAnalysisResult is missing from state");
            }

            QAAnalysisResult analysisResult = context.getQaWorkflowState().getQaAnalysisResult();

            // 1. Fetch Agent
            Agent<AgentRequest, AgentResponse> agent = (Agent<AgentRequest, AgentResponse>) 
                agentManager.getAgentByType(AgentType.TEST_CASE_GENERATOR);
            
            if (agent == null) {
                throw new IllegalStateException("Agent of type TEST_CASE_GENERATOR is not registered in the system");
            }

            // 2. Prepare request and execute agent
            String analysisJson = objectMapper.writeValueAsString(analysisResult);
            AgentRequest agentReq = new AgentRequest();
            agentReq.setPrompt(analysisJson);
            agentReq.getMetadata().setCorrelationId(context.getMetadata().getCorrelationId());

            AgentResponse agentRes = agent.execute(agentReq, new AgentContext());

            if ("FAILED".equals(agentRes.getStatus())) {
                throw new RuntimeException("Test Case Generator Agent failed: " + agentRes.getMessage());
            }

            // 3. Validate, Normalize, and Repair LLM JSON output
            String normalizedJson = responseValidator.validateAndNormalize(AgentType.TEST_CASE_GENERATOR, agentRes.getContent());

            // 4. Deserialize and store suite
            GeneratedTestCaseSuite tcSuite = objectMapper.readValue(normalizedJson, GeneratedTestCaseSuite.class);
            context.getQaWorkflowState().setGeneratedTestCaseSuite(tcSuite);

            response.setStatus("SUCCESS");
            response.setMessage("Successfully generated test case suite");
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("Failed in TestCaseGenerationStep: " + e.getMessage());
        }
        return response;
    }
}
