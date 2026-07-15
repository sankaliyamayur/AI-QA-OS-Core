package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.engine.WorkflowStep;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.LearningResult;
import com.aiqaos.core.model.QAExecutionReport;
import com.aiqaos.core.model.SelfHealingResult;
import com.aiqaos.healing.engine.SelfHealingEngine;
import com.aiqaos.workflow.validation.LLMResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@SuppressWarnings("unchecked")
public class SelfHealingStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private SelfHealingEngine healingEngine;

    @Autowired
    private LLMResponseValidator responseValidator;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "SelfHealingStep";
    }

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            if (context.getQaWorkflowState() == null) {
                throw new IllegalStateException("QA workflow state is missing");
            }

            ExecutionResult executionResult = context.getQaWorkflowState().getExecutionResult();
            if (executionResult == null) {
                // If there's no execution result, bypass
                SelfHealingResult emptyResult = new SelfHealingResult();
                emptyResult.setHealingApplied(false);
                emptyResult.setRecoveryStatus("NO_EXECUTION");
                context.getQaWorkflowState().setSelfHealingResult(emptyResult);
                response.setStatus("SUCCESS");
                response.setMessage("Bypassed self-healing as no execution result is present");
                return response;
            }

            if (executionResult.isSuccess()) {
                // Success execution bypasses healing
                SelfHealingResult emptyResult = new SelfHealingResult();
                emptyResult.setHealingApplied(false);
                emptyResult.setRecoveryStatus("BYPASSED_SUCCESS");
                context.getQaWorkflowState().setSelfHealingResult(emptyResult);
                response.setStatus("SUCCESS");
                response.setMessage("Bypassed self-healing as original execution succeeded");
                return response;
            }

            BugAnalysisReport bugReport = context.getQaWorkflowState().getBugAnalysisReport();
            LearningResult learningResult = context.getQaWorkflowState().getLearningResult();

            // 1. Fetch Agent
            Agent<AgentRequest, AgentResponse> agent = (Agent<AgentRequest, AgentResponse>)
                    agentManager.getAgentByType(AgentType.SELF_HEALING_ENGINEER);

            if (agent == null) {
                throw new IllegalStateException("Agent SELF_HEALING_ENGINEER is not registered");
            }

            // 2. Package request
            Map<String, Object> payload = new HashMap<>();
            payload.put("execution", executionResult);
            payload.put("bugAnalysis", bugReport);
            payload.put("learningResult", learningResult);

            AgentRequest agentReq = new AgentRequest();
            agentReq.setPrompt(objectMapper.writeValueAsString(payload));
            if (context.getMetadata() != null && context.getMetadata().getCorrelationId() != null) {
                agentReq.getMetadata().setCorrelationId(context.getMetadata().getCorrelationId());
            }

            // 3. Execute Agent
            AgentResponse agentRes = agent.execute(agentReq, new AgentContext());
            if ("FAILED".equals(agentRes.getStatus())) {
                throw new RuntimeException("Self-Healing Agent failed: " + agentRes.getMessage());
            }

            // 4. Validate and Normalize response
            String normalizedJson = responseValidator.validateAndNormalize(AgentType.SELF_HEALING_ENGINEER, agentRes.getContent());
            com.fasterxml.jackson.databind.JsonNode decision = objectMapper.readTree(normalizedJson);
            boolean retryRequired = decision.has("retryRequired") ? decision.get("retryRequired").asBoolean() : true;
            String healingAction = decision.has("healingAction") ? decision.get("healingAction").asText() : "RETRY_ONLY";
            String reason = decision.has("reason") ? decision.get("reason").asText() : "Self-healing execution retry";

            SelfHealingResult healingResult;
            if (!retryRequired) {
                healingResult = new SelfHealingResult();
                healingResult.setHealingApplied(false);
                healingResult.setRetrySuccessful(false);
                healingResult.setRecoveryStatus("BYPASSED_DECISION");
                healingResult.setAppliedFix("Bypassed retry based on SelfHealingAgent decision");
            } else {
                // 5. Invoke Engine (which manages retries and updates attempts memory)
                healingResult = healingEngine.heal(executionResult, learningResult, bugReport);
                // Override resolved properties from LLM Decision
                if (healingResult.getActionType() == null || "RETRY_ONLY".equals(healingResult.getActionType())) {
                    healingResult.setActionType(healingAction);
                    healingResult.setAppliedFix(reason);
                }
            }
            context.getQaWorkflowState().setSelfHealingResult(healingResult);

            if (healingResult.isRetrySuccessful()) {
                response.setStatus("SUCCESS");
                response.setMessage("Self-healing applied and retry succeeded!");
                // Override execution status in variables if healed successfully
                if (context.getVariables() != null) {
                    context.getVariables().put("executionStatus", "PASS");
                }

                // Promote the healed execution to be the workflow's execution-of-record, and
                // patch the already-generated report so the final QAExecutionReport reflects
                // the healed outcome rather than the initial failure. The original failed
                // execution remains preserved for audit in healingResult.getOriginalExecution().
                ExecutionResult healedExecution = healingResult.getHealedExecution();
                if (healedExecution != null) {
                    context.getQaWorkflowState().setExecutionResult(healedExecution);
                    patchReportForHealedExecution(context, healingResult);
                    markBugAnalysisResolved(context);
                }
            } else {
                response.setStatus("SUCCESS"); // Fail-safe: step itself returns SUCCESS to keep pipeline running
                response.setMessage("Self-healing retry failed or loop protected: " + healingResult.getRecoveryStatus());
            }

        } catch (Exception e) {
            // Fail-safe design: healing errors must never break the main pipeline
            SelfHealingResult errorResult = new SelfHealingResult();
            errorResult.setHealingApplied(false);
            errorResult.setRecoveryStatus("ERROR: " + e.getMessage());
            context.getQaWorkflowState().setSelfHealingResult(errorResult);
            
            response.setStatus("SUCCESS");
            response.setMessage("Self-healing bypassed due to error: " + e.getMessage());
        }
        return response;
    }

    private void patchReportForHealedExecution(WorkflowContext context, SelfHealingResult healingResult) {
        QAExecutionReport report = context.getQaWorkflowState().getQaExecutionReport();
        ExecutionResult healedExecution = healingResult.getHealedExecution();
        if (report == null || healedExecution == null) {
            return;
        }

        int totalTests = healedExecution.getPassed() + healedExecution.getFailed();

        report.setExecutionResult(healedExecution);
        report.setOverallResult(healedExecution.isSuccess() ? "PASS" : "FAIL");
        report.setTotalTestCases(totalTests);
        report.setPassedTests(healedExecution.getPassed());
        report.setFailedTests(healedExecution.getFailed());
        report.setPassPercentage(totalTests > 0
                ? Math.round((healedExecution.getPassed() / (double) totalTests) * 10000.0) / 100.0
                : 100.0);
        report.setSummary("Execution initially failed (" + describe(healingResult.getOriginalFailure())
                + ") but was successfully recovered via self-healing strategy '"
                + describe(healingResult.getActionType())
                + "'. This report reflects the final, healed execution outcome.");
        report.setStatus("HEALED");

        context.getQaWorkflowState().setQaExecutionReport(report);
    }

    private void markBugAnalysisResolved(WorkflowContext context) {
        BugAnalysisReport bugReport = context.getQaWorkflowState().getBugAnalysisReport();
        if (bugReport == null) {
            return;
        }
        bugReport.setStatus("RESOLVED");
        context.getQaWorkflowState().setBugAnalysisReport(bugReport);
    }

    private String describe(String value) {
        return value != null && !value.trim().isEmpty() ? value : "unspecified";
    }
}
