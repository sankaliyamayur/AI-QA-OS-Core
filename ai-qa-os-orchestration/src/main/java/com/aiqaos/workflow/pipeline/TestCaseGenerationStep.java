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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private com.aiqaos.core.repository.TestCaseRepository testCaseRepo;

    @Autowired
    private com.aiqaos.core.repository.ModuleRepository moduleRepo;

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
            
            // Save generated test cases to the database so they appear in the UI
            {
                String moduleId = resolveModuleId(context);

                // Each run generates its own ids, so without this the module accumulates
                // a fresh near-duplicate suite every time instead of being refreshed.
                List<com.aiqaos.core.entity.TestCaseEntity> previous = testCaseRepo.findByModuleId(moduleId);
                if (!previous.isEmpty()) {
                    testCaseRepo.deleteAll(previous);
                }

                if (tcSuite.getTestCases() != null) {
                    for (com.aiqaos.core.model.GeneratedTestCaseSuite.TestCase tcDto : tcSuite.getTestCases()) {
                        com.aiqaos.core.entity.TestCaseEntity tcEntity = testCaseRepo.findById(tcDto.getId())
                            .orElse(new com.aiqaos.core.entity.TestCaseEntity());
                        tcEntity.setId(tcDto.getId());
                        tcEntity.setModuleId(moduleId);
                        tcEntity.setName(tcDto.getName());
                        tcEntity.setDescription(tcDto.getDescription());
                        tcEntity.setPriority(tcDto.getPriority());
                        tcEntity.setStatus("PENDING");
                        
                        // Populate AI-generated steps as Timeline steps
                        if (tcDto.getSteps() != null && !tcDto.getSteps().isEmpty()) {
                            List<Map<String, Object>> timelineSteps = new ArrayList<>();
                            LocalTime startTime = LocalTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                            int secondsOffset = 1;
                            
                            for (String stepStr : tcDto.getSteps()) {
                                Map<String, Object> step = new HashMap<>();
                                step.put("time", startTime.plusSeconds(secondsOffset).format(formatter));
                                step.put("action", stepStr);
                                step.put("status", "PENDING"); // Will be updated during ExecutionStep
                                step.put("details", "");
                                timelineSteps.add(step);
                                secondsOffset += 2; // Simulate 2 seconds per step
                            }
                            tcEntity.setSteps(timelineSteps);
                        }
                        
                        testCaseRepo.save(tcEntity);
                    }
                }

                // The module carries its own count for the dashboard; leaving it at the
                // previous suite's size would misreport what was just generated.
                moduleRepo.findById(moduleId).ifPresent(module -> {
                    module.setTotalTestCases(tcSuite.getTestCases() == null ? 0 : tcSuite.getTestCases().size());
                    moduleRepo.save(module);
                });
            }

            response.setStatus("SUCCESS");
            response.setMessage("Successfully generated test case suite");
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("Failed in TestCaseGenerationStep: " + e.getMessage());
        }
        return response;
    }

    /**
     * Resolves the owning module from the story path the workflow was started with.
     * test_cases.module_id is a foreign key into modules(id), so the id must be an
     * existing module rather than anything generated at runtime.
     */
    private String resolveModuleId(WorkflowContext context) {
        com.aiqaos.core.requirement.RequirementContext reqContext =
            context.getQaWorkflowState().getRequirementContext();

        String storyPath = reqContext == null ? null : reqContext.getAdditionalMetadata().get("storyPath");

        if (storyPath == null || storyPath.trim().isEmpty()) {
            throw new IllegalStateException("storyPath is missing from RequirementContext; cannot resolve module");
        }

        String normalized = normalizePath(storyPath);
        for (com.aiqaos.core.entity.ModuleEntity module : moduleRepo.findAll()) {
            String requirementPath = module.getRequirementPath();
            if (requirementPath == null || requirementPath.trim().isEmpty()) {
                continue;
            }
            // The workflow is started with an absolute path while modules store a
            // repository-relative one, so compare on the shared trailing segment.
            if (normalized.endsWith(normalizePath(requirementPath))) {
                return module.getId();
            }
        }

        throw new IllegalStateException("No module registered for requirement path: " + storyPath);
    }

    private String normalizePath(String path) {
        return path.replace('\\', '/').toLowerCase();
    }
}
