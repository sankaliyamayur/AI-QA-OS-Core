package com.aiqaos.core.model;

import com.aiqaos.core.enums.HealingActionType;
import java.io.Serializable;

public class SelfHealingRecommendation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String recommendationId;
    private String issue;
    private String suggestedAction;
    private HealingActionType actionType;
    private boolean regenerateScript;
    private boolean updateLocator;
    private boolean updatePrompt;
    private double confidence;

    public SelfHealingRecommendation() {}

    public String getRecommendationId() { return recommendationId; }
    public void setRecommendationId(String recommendationId) { this.recommendationId = recommendationId; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }

    public HealingActionType getActionType() { return actionType; }
    public void setActionType(HealingActionType actionType) { this.actionType = actionType; }

    public boolean isRegenerateScript() { return regenerateScript; }
    public void setRegenerateScript(boolean regenerateScript) { this.regenerateScript = regenerateScript; }

    public boolean isUpdateLocator() { return updateLocator; }
    public void setUpdateLocator(boolean updateLocator) { this.updateLocator = updateLocator; }

    public boolean isUpdatePrompt() { return updatePrompt; }
    public void setUpdatePrompt(boolean updatePrompt) { this.updatePrompt = updatePrompt; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}
