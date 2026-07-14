package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.engine.WorkflowStep;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import com.aiqaos.execution.engine.ExecutionConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class ExecutionStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExecutionEngineFactory engineFactory;

    @Override
    public String getName() {
        return "ExecutionStep";
    }

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            if (context.getQaWorkflowState() == null || context.getQaWorkflowState().getGeneratedScriptSuite() == null) {
                throw new IllegalStateException("GeneratedScriptSuite is missing from state");
            }

            GeneratedScriptSuite scriptSuite = context.getQaWorkflowState().getGeneratedScriptSuite();

            boolean simulateFailure = false;
            if (context.getVariables() != null && Boolean.TRUE.equals(context.getVariables().get("simulateFailure"))) {
                simulateFailure = true;
            }
            if (request != null && request.getInputs() != null && Boolean.TRUE.equals(request.getInputs().get("simulateFailure"))) {
                simulateFailure = true;
            }

            // 1. Fetch Agent
            Agent<AgentRequest, AgentResponse> agent = (Agent<AgentRequest, AgentResponse>) 
                agentManager.getAgentByType(AgentType.EXECUTION_ENGINEER);
            
            if (agent == null) {
                throw new IllegalStateException("Agent of type EXECUTION_ENGINEER is not registered in the system");
            }

            // 2. Prepare request and execute agent
            String scriptsJson = objectMapper.writeValueAsString(scriptSuite);
            AgentRequest agentReq = new AgentRequest();
            agentReq.setPrompt(scriptsJson);
            agentReq.getMetadata().setCorrelationId(context.getMetadata().getCorrelationId());

            AgentResponse agentRes = agent.execute(agentReq, new AgentContext());

            if ("FAILED".equals(agentRes.getStatus())) {
                throw new RuntimeException("Execution Engineer Agent failed: " + agentRes.getMessage());
            }

            // 3. Parse LLM JSON strategy into ExecutionConfiguration
            ExecutionConfiguration config = objectMapper.readValue(agentRes.getContent(), ExecutionConfiguration.class);

            // 4. Resolve appropriate framework ExecutionEngine
            String framework = "Playwright";
            if (scriptSuite.getScripts() != null && !scriptSuite.getScripts().isEmpty()) {
                String scriptFramework = scriptSuite.getScripts().get(0).getFramework();
                if (scriptFramework != null && !scriptFramework.trim().isEmpty()) {
                    framework = scriptFramework;
                }
            }
            ExecutionEngine engine = engineFactory.getEngine(framework);

            // 5. Execute automation scripts via resolved engine
            ExecutionResult execResult = engine.execute(scriptSuite, config);

            // 6. Handle simulated failure override if specified in pipeline parameters
            if (simulateFailure) {
                execResult.setSuccess(false);
                execResult.setStatus("FAILED");
                execResult.setErrorMessage("Element not found: '#submit'");
            }

            context.getQaWorkflowState().setExecutionResult(execResult);

            if (!execResult.isSuccess()) {
                response.setStatus("FAILED");
                response.setMessage("Execution step failed. Triggering self-healing/bug analysis.");
            } else {
                response.setStatus("SUCCESS");
                response.setMessage("All tests executed successfully");
            }
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("Failed in ExecutionStep: " + e.getMessage());
        }
        return response;
    }
}
