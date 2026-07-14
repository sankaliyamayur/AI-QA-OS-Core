package com.aiqaos.core.model;

import java.util.ArrayList;
import java.util.List;

public class LearningFeedback {
    private String feedbackId;
    private String workflowId;
    private List<String> optimizationSuggestions = new ArrayList<>();
    private List<String> updatedKnowledgeNodes = new ArrayList<>();
    private boolean successfullySaved;

    public LearningFeedback() {}

    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public List<String> getOptimizationSuggestions() { return optimizationSuggestions; }
    public void setOptimizationSuggestions(List<String> optimizationSuggestions) { this.optimizationSuggestions = optimizationSuggestions; }

    public List<String> getUpdatedKnowledgeNodes() { return updatedKnowledgeNodes; }
    public void setUpdatedKnowledgeNodes(List<String> updatedKnowledgeNodes) { this.updatedKnowledgeNodes = updatedKnowledgeNodes; }

    public boolean isSuccessfullySaved() { return successfullySaved; }
    public void setSuccessfullySaved(boolean successfullySaved) { this.successfullySaved = successfullySaved; }
}
