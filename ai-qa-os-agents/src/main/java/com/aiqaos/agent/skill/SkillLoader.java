package com.aiqaos.agent.skill;

import com.aiqaos.core.skill.SkillContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Classpath resource loader for AI-QA-OS runtime SKILL.md files.
 * Loads skill instructions from ai-qa-os-agents classpath resources.
 */
@Component
public class SkillLoader implements SkillContract {

    private static final Logger log = LoggerFactory.getLogger(SkillLoader.class);

    @Override
    public Optional<String> loadSkillInstructions(String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            return Optional.empty();
        }

        String resourcePath = "skills/" + skillName.trim() + "/SKILL.md";
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                log.debug("[SkillLoader] Skill resource not found at: {}", resourcePath);
                return Optional.empty();
            }

            try (InputStream is = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                
                String rawContent = reader.lines().collect(Collectors.joining("\n"));
                String bodyContent = stripYamlFrontmatter(rawContent);
                
                if (bodyContent == null || bodyContent.trim().isEmpty()) {
                    return Optional.empty();
                }

                log.debug("[SkillLoader] Successfully loaded skill instructions for: {}", skillName);
                return Optional.of(bodyContent.trim());
            }
        } catch (Exception e) {
            log.warn("[SkillLoader] Failed to load skill instructions for '{}': {}", skillName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Strips YAML frontmatter enclosed between the first two '---' delimiters.
     */
    private String stripYamlFrontmatter(String rawContent) {
        if (rawContent == null) {
            return null;
        }

        String trimmed = rawContent.trim();
        if (trimmed.startsWith("---")) {
            int secondDelimiterIndex = trimmed.indexOf("---", 3);
            if (secondDelimiterIndex != -1) {
                return trimmed.substring(secondDelimiterIndex + 3).trim();
            }
        }
        return rawContent;
    }
}
