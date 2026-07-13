package com.aiqaos.intelligence.loader;

import java.util.Optional;

public interface PromptSource {
    Optional<String> loadPrompt(String templateName, String version);
}