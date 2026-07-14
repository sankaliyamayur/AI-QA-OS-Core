package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.engine.WorkflowStep;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.core.model.QAAnalysisResult;
import com.aiqaos.core.model.QAExecutionReport;
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
public class ReportingStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LLMResponseValidator responseValidator;

    @Override
    public String getName() {
        return "ReportingStep";
    }

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            if (context.getQaWorkflowState() == null) {
                throw new IllegalStateException("QA state is missing from context");
            }

            // Determine overall success for variable assignment (preserved from original stub)
            ExecutionResult execResult = context.getQaWorkflowState().getExecutionResult();
            boolean isSuccess = execResult == null || execResult.isSuccess();

            // Fetch Agent
            Agent<AgentRequest, AgentResponse> agent = (Agent<AgentRequest, AgentResponse>)
                    agentManager.getAgentByType(AgentType.REPORTER);

            if (agent == null) {
                throw new IllegalStateException("Agent of type REPORTER is not registered in the system");
            }

            QAExecutionReport report = null;

            try {
                // Collect all state outputs
                QAAnalysisResult qaAnalysis = context.getQaWorkflowState().getQaAnalysisResult();
                GeneratedTestCaseSuite testSuite = context.getQaWorkflowState().getGeneratedTestCaseSuite();
                GeneratedScriptSuite scriptSuite = context.getQaWorkflowState().getGeneratedScriptSuite();
                BugAnalysisReport bugReport = context.getQaWorkflowState().getBugAnalysisReport();

                // Package workflow data payload
                Map<String, Object> workflowData = new HashMap<>();
                workflowData.put("analysis", qaAnalysis);
                workflowData.put("testCases", testSuite);
                workflowData.put("scripts", scriptSuite);
                workflowData.put("execution", execResult);
                workflowData.put("bugs", bugReport);

                String workflowDataJson;
                try {
                    workflowDataJson = objectMapper.writeValueAsString(workflowData);
                } catch (Exception serEx) {
                    workflowDataJson = "{\"error\":\"Context serialization failed\",\"errorMessage\":\""
                            + serEx.getMessage() + "\"}";
                }

                AgentRequest agentReq = new AgentRequest();
                agentReq.setPrompt(workflowDataJson);
                UUID corrId = context.getMetadata() != null ? context.getMetadata().getCorrelationId() : null;
                if (corrId != null) {
                    agentReq.getMetadata().setCorrelationId(corrId);
                }

                // Execute agent
                AgentResponse agentRes = agent.execute(agentReq, new AgentContext());

                if ("FAILED".equals(agentRes.getStatus())) {
                    throw new RuntimeException("Reporting Agent failed: " + agentRes.getMessage());
                }

                // Validate and normalize
                String normalizedJson = responseValidator.validateAndNormalize(AgentType.REPORTER, agentRes.getContent());

                // Deserialize to QAExecutionReport
                report = objectMapper.readValue(normalizedJson, QAExecutionReport.class);

                // Enrich with nested state objects (they are not serialized by LLM)
                report.setQaAnalysisResult(qaAnalysis);
                report.setTestSuite(testSuite);
                report.setScriptSuite(scriptSuite);
                report.setExecutionResult(execResult);
                report.setBugAnalysisReport(bugReport);

            } catch (Exception e) {
                // Fail-safe fallback — reporting failure must never stop the pipeline
                report = buildFallbackReport(execResult, e.getMessage());
            }

            context.getQaWorkflowState().setQaExecutionReport(report);

            // Preserved variable assignments from original step
            context.getVariables().put("reportCompiled", true);
            context.getVariables().put("executionStatus", isSuccess ? "PASS" : "FAIL");

            response.setStatus("SUCCESS");
            response.setMessage("Successfully compiled and distributed workflow execution reports");
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("Failed in ReportingStep: " + e.getMessage());
        }
        return response;
    }

    private QAExecutionReport buildFallbackReport(ExecutionResult execResult, String errorMessage) {
        QAExecutionReport fallback = new QAExecutionReport();
        fallback.setReportId("REPORT-FALLBACK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        fallback.setReportVersion("v1.0");
        fallback.setStatus("PARTIAL");
        fallback.setSummary("Partial report generated due to agent failure: " + errorMessage);
        fallback.setGeneratedBy("AI-QA-OS ReportingAgent (fallback)");
        fallback.setCreatedTime(LocalDateTime.now());

        if (execResult != null) {
            fallback.setTotalTestCases(execResult.getPassed() + execResult.getFailed());
            fallback.setPassedTests(execResult.getPassed());
            fallback.setFailedTests(execResult.getFailed());
            int total = execResult.getPassed() + execResult.getFailed();
            if (total > 0) {
                fallback.setPassPercentage(
                    Math.round((execResult.getPassed() / (double) total) * 10000.0) / 100.0);
            }
            fallback.setOverallResult(execResult.isSuccess() ? "PASS" : "FAIL");
            fallback.setExecutionResult(execResult);
        } else {
            fallback.setOverallResult("UNKNOWN");
        }

        return fallback;
    }
}

