package com.aiqaos.intelligence.loader;

import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class GitPromptSource implements PromptSource {
    @Override
    public Optional<String> loadPrompt(String templateName, String version) {
        // Stub implementation for git pull fetches
        return Optional.empty();
    }
}