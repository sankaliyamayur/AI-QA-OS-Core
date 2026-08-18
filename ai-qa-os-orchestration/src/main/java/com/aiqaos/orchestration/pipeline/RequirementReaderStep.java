package com.aiqaos.orchestration.pipeline;

import com.aiqaos.core.engine.WorkflowStep;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.requirement.RequirementContext;
import com.aiqaos.core.requirement.RequirementParser;
import com.aiqaos.core.requirement.RequirementReader;
import com.aiqaos.core.enums.WorkflowStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequirementReaderStep implements WorkflowStep<WorkflowRequest, WorkflowResponse> {

    @Autowired
    private RequirementReader reader;

    @Autowired
    private RequirementParser parser;

    @Override
    public String getName() {
        return "RequirementReaderStep";
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequirementReaderStep.class);

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            String storyPath = null;
            if (context.getVariables() != null && context.getVariables().containsKey("storyPath")) {
                storyPath = (String) context.getVariables().get("storyPath");
            }
            if (storyPath == null && request != null && request.getInputs() != null) {
                if (request.getInputs().containsKey("storyPath")) {
                    storyPath = (String) request.getInputs().get("storyPath");
                } else if (request.getInputs().containsKey("requirementPath")) {
                    storyPath = (String) request.getInputs().get("requirementPath");
                }
            }

            if (storyPath == null || storyPath.trim().isEmpty()) {
                throw new IllegalArgumentException("storyPath or requirementPath input is missing from request or variables");
            }

            log.info("[RequirementReaderStep] Reading requirement story from path: {}", storyPath);
            String content = reader.readRequirement(storyPath);
            RequirementContext reqContext = parser.parse(content);

            // Record the resolved path so downstream steps can resolve the owning module
            reqContext.getAdditionalMetadata().put("storyPath", storyPath);

            if (context.getQaWorkflowState() == null) {
                context.setQaWorkflowState(new AutonomousQAWorkflowState());
            }
            context.getQaWorkflowState().setRequirementContext(reqContext);
            context.setStatus(WorkflowStatus.RUNNING);

            response.setStatus("SUCCESS");
            response.setMessage("Successfully read and parsed user story");
            log.info("[RequirementReaderStep] Requirement parsed successfully for title: {}", reqContext.getTitle());
        } catch (Exception e) {
            log.error("[RequirementReaderStep] Failed to read or parse requirement: {}", e.getMessage(), e);
            context.setStatus(WorkflowStatus.FAILED);
            response.setStatus("FAILED");
            response.setMessage("Failed in RequirementReaderStep: " + e.getMessage());
        }
        return response;
    }
}
