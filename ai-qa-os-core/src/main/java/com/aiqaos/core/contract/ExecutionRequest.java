package com.aiqaos.core.contract;

import java.util.HashMap;
import java.util.Map;

public class ExecutionRequest extends BaseRequest {
    private String scriptContent;
    private String runtimeLanguage;
    private Map<String, String> environmentVariables = new HashMap<>();

    public String getScriptContent() { return scriptContent; }
    public void setScriptContent(String scriptContent) { this.scriptContent = scriptContent; }
    public String getRuntimeLanguage() { return runtimeLanguage; }
    public void setRuntimeLanguage(String runtimeLanguage) { this.runtimeLanguage = runtimeLanguage; }
    public Map<String, String> getEnvironmentVariables() { return environmentVariables; }
    public void setEnvironmentVariables(Map<String, String> environmentVariables) { this.environmentVariables = environmentVariables; }
}