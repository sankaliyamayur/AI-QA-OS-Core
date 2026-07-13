package com.aiqaos.intelligence.component;

import org.springframework.stereotype.Component;

@Component
public class DefaultPromptFormatter implements LLMPromptFormatter {
    @Override
    public String format(String systemInstruction, String userPrompt) {
        StringBuilder builder = new StringBuilder();
        if (systemInstruction != null && !systemInstruction.isEmpty()) {
            builder.append("System: ").append(systemInstruction).append("\n\n");
        }
        builder.append("User: ").append(userPrompt);
        return builder.toString();
    }
}