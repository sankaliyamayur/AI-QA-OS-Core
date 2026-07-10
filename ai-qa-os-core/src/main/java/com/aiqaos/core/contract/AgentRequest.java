package com.aiqaos.core.contract;

public class AgentRequest extends BaseRequest {
    private String prompt;
    private String systemInstruction;

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getSystemInstruction() { return systemInstruction; }
    public void setSystemInstruction(String systemInstruction) { this.systemInstruction = systemInstruction; }
}