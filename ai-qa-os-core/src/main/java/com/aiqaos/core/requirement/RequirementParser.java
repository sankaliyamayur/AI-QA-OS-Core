package com.aiqaos.core.requirement;

import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class RequirementParser {

    public RequirementContext parse(String rawContent) {
        if (rawContent == null) {
            return new RequirementContext("");
        }

        RequirementContext context = new RequirementContext(rawContent);
        List<String> acceptanceCriteria = new ArrayList<>();
        StringBuilder descriptionBuilder = new StringBuilder();
        String title = "Untitled Story";

        try (BufferedReader reader = new BufferedReader(new StringReader(rawContent))) {
            String line;
            boolean inAcceptanceSection = false;
            boolean titleFound = false;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                // Format 1 & 2: Key-value checks
                String lower = trimmedLine.toLowerCase();
                if (lower.startsWith("title:")) {
                    title = trimmedLine.substring(6).trim();
                    titleFound = true;
                    inAcceptanceSection = false;
                    continue;
                }
                if (lower.startsWith("description:")) {
                    descriptionBuilder.append(trimmedLine.substring(12).trim());
                    inAcceptanceSection = false;
                    continue;
                }
                if (lower.startsWith("acceptance criteria")) {
                    inAcceptanceSection = true;
                    continue;
                }

                // Detect Markdown headers
                if (trimmedLine.startsWith("#")) {
                    if (lower.contains("acceptance") || lower.contains("criteria")) {
                        inAcceptanceSection = true;
                    } else {
                        inAcceptanceSection = false;
                        if (trimmedLine.startsWith("# ") && !titleFound) {
                            title = trimmedLine.substring(2).trim();
                            titleFound = true;
                        } else if (trimmedLine.startsWith("## ") && !titleFound) {
                            title = trimmedLine.substring(3).trim();
                            titleFound = true;
                        }
                    }
                    continue;
                }

                // Process lines based on section
                if (inAcceptanceSection) {
                    // Extract criteria (bullet points, numbered points, or plain text criteria)
                    if (trimmedLine.startsWith("-") || trimmedLine.startsWith("*")) {
                        String criterion = trimmedLine.replaceAll("^[-*]\\s*(?:\\[\\s*\\]|\\([\\s*]\\))?\\s*", "").trim();
                        if (!criterion.isEmpty()) {
                            acceptanceCriteria.add(criterion);
                        }
                    } else if (!trimmedLine.endsWith(":")) {
                        // In US-001.md, plain lines like 'The login page should have fields...'
                        acceptanceCriteria.add(trimmedLine);
                    }
                } else {
                    if (descriptionBuilder.length() > 0) {
                        descriptionBuilder.append("\n");
                    }
                    descriptionBuilder.append(trimmedLine);
                }
            }
        } catch (Exception e) {
            // Fallback: parse failed
        }

        context.setTitle(title);
        context.setDescription(descriptionBuilder.toString());
        context.setAcceptanceCriteria(acceptanceCriteria);

        return context;
    }
}
