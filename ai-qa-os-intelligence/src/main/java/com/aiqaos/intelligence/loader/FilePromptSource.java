package com.aiqaos.intelligence.loader;

import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class FilePromptSource implements PromptSource {
    @Override
    public Optional<String> loadPrompt(String templateName, String version) {
        // Look up under prompts/
        String resourcePath = "/prompts/" + templateName + "_" + version + ".md";
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                return Optional.of(new String(bytes, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            // Ignore and fall back
        }
        return Optional.empty();
    }
}