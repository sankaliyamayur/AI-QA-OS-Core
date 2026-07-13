package com.aiqaos.intelligence.component;

import org.springframework.stereotype.Component;
import com.aiqaos.core.exception.ValidationException;

@Component
public class PromptSecurityGuard {
    public void scan(String rawPrompt) {
        if (rawPrompt == null) return;
        String lower = rawPrompt.toLowerCase();
        if (lower.contains("ignore previous instructions") || 
            lower.contains("system override") || 
            lower.contains("jailbreak")) {
            throw new ValidationException("Potential Prompt Injection / Jailbreak attempt detected.");
        }
    }
}