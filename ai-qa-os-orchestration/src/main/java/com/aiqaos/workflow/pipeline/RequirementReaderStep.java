package com.aiqaos.workflow.pipeline;

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

    @Override
    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        WorkflowResponse response = new WorkflowResponse();
        try {
            String storyPath = null;
            if (context.getVariables() != null && context.getVariables().containsKey("storyPath")) {
                storyPath = (String) context.getVariables().get("storyPath");
            }
            if (storyPath == null && request != null && request.getInputs() != null && request.getInputs().containsKey("storyPath")) {
                storyPath = (String) request.getInputs().get("storyPath");
            }

            if (storyPath == null || storyPath.trim().isEmpty()) {
                throw new IllegalArgumentException("storyPath input is missing from request or variables");
            }

            String content = reader.readRequirement(storyPath);
            RequirementContext reqContext = parser.parse(content);

            if (context.getQaWorkflowState() == null) {
                context.setQaWorkflowState(new AutonomousQAWorkflowState());
            }
            context.getQaWorkflowState().setRequirementContext(reqContext);
            context.setStatus(WorkflowStatus.RUNNING);

            response.setStatus("SUCCESS");
            response.setMessage("Successfully read and parsed user story");
        } catch (Exception e) {
            context.setStatus(WorkflowStatus.FAILED);
            response.setStatus("FAILED");
            response.setMessage("Failed in RequirementReaderStep: " + e.getMessage());
        }
        return response;
    }
}
