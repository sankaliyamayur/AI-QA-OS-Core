package com.aiqaos.intelligence.component;

public interface LLMPromptFormatter {
    String format(String systemInstruction, String userPrompt);
}