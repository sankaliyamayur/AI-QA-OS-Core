package com.aiqaos.core.contract;

import java.util.HashMap;
import java.util.Map;

public class PromptRequest extends BaseRequest {
    private String templateName;
    private Map<String, Object> parameters = new HashMap<>();

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}