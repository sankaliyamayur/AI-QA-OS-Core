package com.aiqaos.core.event;

public class PromptEvent extends BaseEvent {
    private String evaluatedTemplate;

    public String getEvaluatedTemplate() { return evaluatedTemplate; }
    public void setEvaluatedTemplate(String evaluatedTemplate) { this.evaluatedTemplate = evaluatedTemplate; }
}