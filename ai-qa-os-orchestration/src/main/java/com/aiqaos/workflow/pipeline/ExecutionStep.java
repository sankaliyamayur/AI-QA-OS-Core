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

    @Autowired
    private com.aiqaos.execution.repository.ExecutionRepository executionRepo;

    @Autowired
    private com.aiqaos.execution.repository.ExecutionArtifactRepository artifactRepo;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecutionStep.class);

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
            ExecutionConfiguration config;
            try {
                config = objectMapper.readValue(agentRes.getContent(), ExecutionConfiguration.class);
            } catch (Exception ex) {
                log.warn("[ExecutionStep] Failed to parse ExecutionConfiguration from agent response. Falling back to default configuration. Error: {}", ex.getMessage());
                config = new ExecutionConfiguration();
            }

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

            // 7. Save Suite-Level Execution details in database
            try {
                com.aiqaos.execution.entity.ExecutionEntity execEntity = new com.aiqaos.execution.entity.ExecutionEntity();
                execEntity.setExecutionId(context.getMetadata().getExecutionId());
                execEntity.setWorkflowId(context.getMetadata().getWorkflowId());
                execEntity.setProvider(execResult.getAgentId() != null ? execResult.getAgentId() : "Playwright");
                execEntity.setEnvironment(config.getEnvironment() != null ? config.getEnvironment().name() : "DEV");
                execEntity.setBrowser(config.getBrowser() != null ? config.getBrowser().name() : "CHROME");
                execEntity.setStatus(execResult.getStatus() != null ? execResult.getStatus() : (execResult.isSuccess() ? "PASSED" : "FAILED"));
                execEntity.setStartTime(execResult.getStartTime() != null ? execResult.getStartTime() : java.time.LocalDateTime.now());
                execEntity.setEndTime(execResult.getCompletedAt() != null ? execResult.getCompletedAt() : java.time.LocalDateTime.now());
                execEntity.setDuration(execResult.getDuration());
                executionRepo.save(execEntity);
                log.info("[ExecutionStep] Saved ExecutionEntity details successfully");
            } catch (Exception ex) {
                log.error("[ExecutionStep] Failed to save ExecutionEntity: {}", ex.getMessage(), ex);
            }

            // 8. Save Test-Case-Level Artifact paths in database
            if (execResult.getArtifacts() != null && !execResult.getArtifacts().isEmpty()) {
                try {
                    java.util.Map<String, com.aiqaos.execution.entity.ExecutionArtifactEntity> artifactMap = new java.util.HashMap<>();

                    for (String artifactStr : execResult.getArtifacts()) {
                        // format parsed from PlaywrightExecutionEngine: type:testCaseId=path
                        if (artifactStr.contains(":") && artifactStr.contains("=")) {
                            String[] parts1 = artifactStr.split(":", 2);
                            String type = parts1[0];
                            String[] parts2 = parts1[1].split("=", 2);
                            String testCaseId = parts2[0];
                            String path = parts2[1];

                            com.aiqaos.execution.entity.ExecutionArtifactEntity artEntity = artifactMap.get(testCaseId);
                            if (artEntity == null) {
                                artEntity = new com.aiqaos.execution.entity.ExecutionArtifactEntity();
                                artEntity.setExecutionId(context.getMetadata().getExecutionId());
                                artEntity.setTestCaseId(testCaseId);
                                artEntity.setBrowser(config.getBrowser() != null ? config.getBrowser().name().toLowerCase() : "chromium");

                                // Calculate the historical run number for this test case
                                int nextRun = 1;
                                try {
                                    java.util.List<com.aiqaos.execution.entity.ExecutionArtifactEntity> prevs = 
                                        artifactRepo.findByTestCaseIdOrderByRunNumberDesc(testCaseId);
                                    if (prevs != null && !prevs.isEmpty()) {
                                        nextRun = prevs.get(0).getRunNumber() + 1;
                                    }
                                } catch (Exception ex) {
                                    log.warn("[ExecutionStep] Could not fetch run history for {}: {}", testCaseId, ex.getMessage());
                                }
                                artEntity.setRunNumber(nextRun);
                                artifactMap.put(testCaseId, artEntity);
                            }

                            if ("screenshot".equalsIgnoreCase(type)) {
                                artEntity.setScreenshotPath(path);
                            } else if ("video".equalsIgnoreCase(type)) {
                                artEntity.setVideoPath(path);
                            } else if ("log".equalsIgnoreCase(type)) {
                                artEntity.setLogPath(path);
                            }
                        }
                    }

                    // Persist all populated artifacts
                    for (com.aiqaos.execution.entity.ExecutionArtifactEntity art : artifactMap.values()) {
                        art.setReportPath(execResult.getReportLocation());
                        artifactRepo.save(art);
                        log.info("[ExecutionStep] Saved Playwright artifacts in DB: TC={}, Screenshot={}, Video={}, Run=#{}",
                                 art.getTestCaseId(), art.getScreenshotPath() != null, art.getVideoPath() != null, art.getRunNumber());
                    }
                } catch (Exception ex) {
                    log.error("[ExecutionStep] Failed to save ExecutionArtifacts: {}", ex.getMessage(), ex);
                }
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
