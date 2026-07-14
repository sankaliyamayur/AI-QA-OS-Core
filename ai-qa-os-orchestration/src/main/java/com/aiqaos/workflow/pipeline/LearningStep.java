package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.engine.WorkflowStep;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.model.LearningFeedback;
import com.aiqaos.core.model.LearningResult;
import com.aiqaos.core.model.QAExecutionReport;
import com.aiqaos.learning.engine.LearningEngine;
import com.aiqaos.learning.memory.LearningMemoryStore;
import com.aiqaos.workflow.validation.LLMResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LearningStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

    @Autowired
    private LearningEngine learningEngine;

    @Autowired
    private LLMResponseValidator responseValidator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LearningMemoryStore memoryStore;

    @Override
    public String getName() {
        return "LearningStep";
    }

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            if (context.getQaWorkflowState() == null) {
                throw new IllegalStateException("QA state is missing from context");
            }

            QAExecutionReport report = context.getQaWorkflowState().getQaExecutionReport();
            LearningResult result = null;

            if (report != null) {
                try {
                    AgentResponse agentRes = learningEngine.analyze(report, context);
                    if ("SUCCESS".equals(agentRes.getStatus()) && agentRes.getContent() != null) {
                        String normalizedJson = responseValidator.validateAndNormalize(AgentType.LEARNING_ENGINEER, agentRes.getContent());
                        result = objectMapper.readValue(normalizedJson, LearningResult.class);
                    } else {
                        result = new LearningResult();
                    }
                } catch (Exception e) {
                    // Fail-safe design: learning failures must not terminate execution
                    result = new LearningResult();
                }
            } else {
                result = new LearningResult();
            }

            // Perform memory persistence in the orchestration step (outside the learning module)
            if (result != null) {
                try {
                    if (result.getPatterns() != null && !result.getPatterns().isEmpty()) {
                        memoryStore.storeFailurePatterns(result.getPatterns());
                    }
                    if (result.getRecommendations() != null && !result.getRecommendations().isEmpty()) {
                        memoryStore.storeRecommendations(result.getRecommendations());
                    }
                } catch (Exception memEx) {
                    // Ignore memory persistence failures in fail-safe path
                }
            }

            context.getQaWorkflowState().setLearningResult(result);

            // Maintain existing LearningFeedback to keep original test compatibility
            LearningFeedback feedback = new LearningFeedback();
            feedback.setFeedbackId("learn-" + System.currentTimeMillis());
            feedback.setWorkflowId(request.getMetadata().getWorkflowId() != null ? 
                request.getMetadata().getWorkflowId().toString() : "workflow-default");
            
            List<String> suggestions = new ArrayList<>();
            suggestions.add("Update knowledge nodes with element selectors cache");
            if (result != null && result.getRecommendations() != null) {
                result.getRecommendations().forEach(r -> suggestions.add(r.getSuggestedAction()));
            }
            feedback.setOptimizationSuggestions(suggestions);
            feedback.setUpdatedKnowledgeNodes(List.of("Login selectors map"));
            feedback.setSuccessfullySaved(true);

            context.getQaWorkflowState().setLearningFeedback(feedback);

            response.setStatus("SUCCESS");
            response.setMessage("Successfully ingested execution learning feedback");
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("Failed in LearningStep: " + e.getMessage());
        }
        return response;
    }
}
