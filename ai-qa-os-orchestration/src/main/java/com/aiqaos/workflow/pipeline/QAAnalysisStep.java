package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.engine.WorkflowStep;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.model.QAAnalysisResult;
import com.aiqaos.core.requirement.RequirementContext;
import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AgentContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class QAAnalysisStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "QAAnalysisStep";
    }

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            if (context.getQaWorkflowState() == null || context.getQaWorkflowState().getRequirementContext() == null) {
                throw new IllegalStateException("RequirementContext is missing from state");
            }

            RequirementContext reqContext = context.getQaWorkflowState().getRequirementContext();

            // 1. Retrieve agent of type QA_ENGINEER
            Agent<AgentRequest, AgentResponse> agent = (Agent<AgentRequest, AgentResponse>) 
                agentManager.getAgentByType(AgentType.QA_ENGINEER);
            
            if (agent == null) {
                throw new IllegalStateException("Agent of type QA_ENGINEER is not registered in the system");
            }

            // 2. Call agent
            AgentRequest agentReq = new AgentRequest();
            agentReq.setPrompt(reqContext.getRawContent());
            agentReq.getMetadata().setCorrelationId(context.getMetadata().getCorrelationId());

            AgentResponse agentRes = agent.execute(agentReq, new AgentContext());

            if ("FAILED".equals(agentRes.getStatus())) {
                throw new RuntimeException("Agent execution failed: " + agentRes.getMessage());
            }

            // 3. Parse and convert output
            // The analysis must come from the agent — never substituted with canned scenarios,
            // otherwise the whole downstream suite is generated from data nobody produced.
            // Tolerate extra fields the model may add, but never invent the analysis itself.
            QAAnalysisResult analysisResult = objectMapper
                .readerFor(QAAnalysisResult.class)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(agentRes.getContent());

            analysisResult.setWorkflowId(request.getMetadata().getWorkflowId() != null ? 
                request.getMetadata().getWorkflowId().toString() : "workflow-default");

            context.getQaWorkflowState().setQaAnalysisResult(analysisResult);

            response.setStatus("SUCCESS");
            response.setMessage("Successfully completed QA Analysis via QAAnalystAgent");
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("Failed in QAAnalysisStep: " + e.getMessage());
        }
        return response;
    }
}
