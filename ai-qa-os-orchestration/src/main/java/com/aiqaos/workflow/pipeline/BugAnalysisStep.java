package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.engine.WorkflowStep;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.core.model.GeneratedScriptSuite;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@SuppressWarnings("unchecked")
public class BugAnalysisStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LLMResponseValidator responseValidator;

    @Override
    public String getName() {
        return "BugAnalysisStep";
    }

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            if (context.getQaWorkflowState() == null || context.getQaWorkflowState().getExecutionResult() == null) {
                throw new IllegalStateException("ExecutionResult is missing from state");
            }

            ExecutionResult execResult = context.getQaWorkflowState().getExecutionResult();

            // 1. Skip step if execution was completely successful
            if (execResult.isSuccess() && execResult.getFailed() == 0) {
                BugAnalysisReport emptyReport = new BugAnalysisReport();
                emptyReport.setReportId("bug-report-empty-" + java.util.UUID.randomUUID().toString().substring(0, 8));
                emptyReport.setRootCause("No failures detected during execution");
                emptyReport.setFailureCategory("NONE");
                emptyReport.setConfidence(1.0);
                emptyReport.setRequiresRegeneration(false);
                emptyReport.setCreatedTime(LocalDateTime.now());
                emptyReport.setSeverity("LOW");
                emptyReport.setPriority("P3");
                emptyReport.setStatus("CLOSED");

                context.getQaWorkflowState().setBugAnalysisReport(emptyReport);
                response.setStatus("SUCCESS");
                response.setMessage("Skipped BugAnalysisStep: execution was successful");
                return response;
            }

            // 2. Fetch Agent
            Agent<AgentRequest, AgentResponse> agent = (Agent<AgentRequest, AgentResponse>)
                agentManager.getAgentByType(AgentType.BUG_ANALYZER);

            if (agent == null) {
                throw new IllegalStateException("Agent of type BUG_ANALYZER is not registered in the system");
            }

            BugAnalysisReport bugReport = null;

            try {
                // 3. Package full execution context
                GeneratedTestCaseSuite tcSuite = context.getQaWorkflowState().getGeneratedTestCaseSuite();
                GeneratedScriptSuite scriptSuite = context.getQaWorkflowState().getGeneratedScriptSuite();

                Map<String, Object> promptContext = new HashMap<>();
                promptContext.put("executionResult", execResult);
                promptContext.put("generatedScripts", scriptSuite);
                promptContext.put("testCases", tcSuite);

                String promptContextJson;
                try {
                    promptContextJson = objectMapper.writeValueAsString(promptContext);
                } catch (Exception serEx) {
                    // Fallback: pass a plain text summary if full serialization fails
                    promptContextJson = "{\"error\":\"Context serialization failed\",\"errorMessage\":\""
                            + serEx.getMessage() + "\"}";
                }

                AgentRequest agentReq = new AgentRequest();
                agentReq.setPrompt(promptContextJson);
                UUID corrId = context.getMetadata() != null ? context.getMetadata().getCorrelationId() : null;
                if (corrId != null) {
                    agentReq.getMetadata().setCorrelationId(corrId);
                }

                // 4. Execute agent
                AgentResponse agentRes = agent.execute(agentReq, new AgentContext());

                if ("FAILED".equals(agentRes.getStatus())) {
                    throw new RuntimeException("Bug Analyzer Agent failed: " + agentRes.getMessage());
                }

                // 5. Validate and Normalize Response
                String normalizedJson = responseValidator.validateAndNormalize(AgentType.BUG_ANALYZER, agentRes.getContent());

                // 6. Deserialize report
                bugReport = objectMapper.readValue(normalizedJson, BugAnalysisReport.class);

            } catch (Exception e) {
                // 7. Fail-safe Fallback Report — pipeline must always continue
                bugReport = new BugAnalysisReport();
                bugReport.setReportId("bug-report-fallback-" + java.util.UUID.randomUUID().toString().substring(0, 8));
                bugReport.setFailureCategory("AGENT_FAILURE");
                bugReport.setConfidence(0.0);
                bugReport.setRequiresRegeneration(false);
                bugReport.setCreatedTime(LocalDateTime.now());
                bugReport.setSeverity("HIGH");
                bugReport.setPriority("P1");
                bugReport.setStatus("OPEN");
                bugReport.setRootCause("Agent execution failed: " + e.getMessage());
                bugReport.setFailureReason("Agent execution failed: " + e.getMessage());
                bugReport.setRecommendation("Verify agent status and LLM availability");
                bugReport.setSelfHealingSuggestion("No self-healing available due to agent failure.");
            }

            context.getQaWorkflowState().setBugAnalysisReport(bugReport);
            response.setStatus("SUCCESS");
            response.setMessage("Successfully analyzed execution failure");
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("Failed in BugAnalysisStep: " + e.getMessage());
        }
        return response;
    }
}
