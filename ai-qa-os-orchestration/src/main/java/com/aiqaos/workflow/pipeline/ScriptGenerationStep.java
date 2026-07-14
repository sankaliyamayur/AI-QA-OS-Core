package com.aiqaos.workflow.pipeline;

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
import com.aiqaos.workflow.validation.LLMResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class ScriptGenerationStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

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

            // 2. Prepare request and execute agent
            String tcSuiteJson = objectMapper.writeValueAsString(tcSuite);
            AgentRequest agentReq = new AgentRequest();
            agentReq.setPrompt(tcSuiteJson);
            agentReq.getMetadata().setCorrelationId(context.getMetadata().getCorrelationId());

            AgentResponse agentRes = agent.execute(agentReq, new AgentContext());

            if ("FAILED".equals(agentRes.getStatus())) {
                throw new RuntimeException("Script Generator Agent failed: " + agentRes.getMessage());
            }

            // 3. Validate and Normalize Response
            String normalizedJson = responseValidator.validateAndNormalize(AgentType.SCRIPT_GENERATOR, agentRes.getContent());

            // 4. Deserialize and store suite
            GeneratedScriptSuite scriptSuite = objectMapper.readValue(normalizedJson, GeneratedScriptSuite.class);
            context.getQaWorkflowState().setGeneratedScriptSuite(scriptSuite);

            response.setStatus("SUCCESS");
            response.setMessage("Successfully generated automation scripts");
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("Failed in ScriptGenerationStep: " + e.getMessage());
        }
        return response;
    }
}
