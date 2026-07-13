package com.aiqaos.intelligence.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class PromptLoader {

    @Autowired
    private List<PromptSource> promptSources;

    public String loadTemplate(String name, String version) {
        for (PromptSource source : promptSources) {
            Optional<String> content = source.loadPrompt(name, version);
            if (content.isPresent()) {
                return content.get();
            }
        }
        throw new com.aiqaos.core.exception.NotFoundException(
            "Prompt template not found: " + name + " (version: " + version + ")"
        );
    }
}