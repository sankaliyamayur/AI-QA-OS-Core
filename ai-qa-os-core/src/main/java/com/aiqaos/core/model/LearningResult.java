package com.aiqaos.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LearningResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<FailurePattern> patterns = new ArrayList<>();
    private List<SelfHealingRecommendation> recommendations = new ArrayList<>();
    private List<LearningEvent> events = new ArrayList<>();

    public LearningResult() {}

    public List<FailurePattern> getPatterns() { return patterns; }
    public void setPatterns(List<FailurePattern> patterns) { this.patterns = patterns; }

    public List<SelfHealingRecommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<SelfHealingRecommendation> recommendations) { this.recommendations = recommendations; }

    public List<LearningEvent> getEvents() { return events; }
    public void setEvents(List<LearningEvent> events) { this.events = events; }
}
