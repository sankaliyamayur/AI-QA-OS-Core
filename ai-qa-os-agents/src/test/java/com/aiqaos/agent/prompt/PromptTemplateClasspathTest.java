package com.aiqaos.agent.prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.intelligence.component.PromptTemplateEngine;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

/**
 * Each agent prompt must exist exactly once on the classpath, and must render with the parameter
 * key its agent actually supplies.
 *
 * <p><b>Why.</b> Two modules shipped templates at the same path {@code /prompts/*.md} —
 * {@code ai-qa-os-agents} (MOD-3's documented location) and {@code ai-qa-os-intelligence}. The
 * intelligence jar sorted earlier on the gateway classpath, so its copies won every lookup. They
 * used the spaced {@code {{ story }}} form while the engine substituted only {@code {{story}}}, so
 * nothing was replaced and no error surfaced. Every agent then prompted the model with a literal
 * placeholder in place of the content, and the model invented answers that looked entirely
 * plausible.
 *
 * <p>Two failure modes, so two assertions: duplicate resources (the shadowing) and key/placeholder
 * drift (the mismatch). This test is in {@code ai-qa-os-agents} because it depends on
 * {@code ai-qa-os-intelligence}, so both candidate copies are visible here.
 */
class PromptTemplateClasspathTest {

    private static List<URL> copiesOf(String template) throws Exception {
        List<URL> urls = new ArrayList<>();
        var found = PromptTemplateClasspathTest.class.getClassLoader()
                .getResources("prompts/" + template + "_latest.md");
        Collections.list(found).forEach(urls::add);
        return urls;
    }

    private static String read(String template) throws Exception {
        try (InputStream is = PromptTemplateClasspathTest.class
                .getResourceAsStream("/prompts/" + template + "_latest.md")) {
            assertNotNull(is, template + " must exist on the classpath");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Each agent's params key, paired with the template it renders. */
    @ParameterizedTest(name = "{0} renders with param \"{1}\"")
    @CsvSource({
            "QA_ANALYSIS,          story",
            "TEST_CASE_GENERATION, analysis",
            "SCRIPT_GENERATION,    testCases",
            "EXECUTION,            scripts",
            "BUG_ANALYSIS,         context",
            "REPORT_GENERATION,    workflowData",
            "LEARNING_ENGINE,      context",
            "SELF_HEALING,         context"
    })
    void everyTemplateIsUniqueAndRendersWithItsAgentsKey(String template, String paramKey) throws Exception {
        List<URL> copies = copiesOf(template);
        assertEquals(1, copies.size(),
                template + " must exist exactly once on the classpath — a second copy silently "
                        + "shadows the first and nothing reports it. Found: " + copies);

        String rendered = new PromptTemplateEngine()
                .render(read(template), Map.of(paramKey, "SENTINEL-VALUE"));

        assertTrue(rendered.contains("SENTINEL-VALUE"),
                template + " did not substitute {{" + paramKey + "}} — the model would receive "
                        + "instructions with no content and invent an answer");
        assertEquals("", PromptTemplateEngine.findUnresolvedPlaceholders(rendered),
                template + " still has unresolved placeholders after rendering");
    }

    @Test
    @DisplayName("the shadowing duplicate set is gone from ai-qa-os-intelligence")
    void noDuplicatePromptResourcesRemain() throws Exception {
        List<String> duplicated = new ArrayList<>();
        for (String t : List.of("QA_ANALYSIS", "TEST_CASE_GENERATION", "SCRIPT_GENERATION",
                "EXECUTION", "BUG_ANALYSIS", "REPORT_GENERATION", "LEARNING_ENGINE", "SELF_HEALING")) {
            if (copiesOf(t).size() > 1) {
                duplicated.add(t);
            }
        }
        assertTrue(duplicated.isEmpty(), "templates present more than once on the classpath: " + duplicated);
    }

    @Test
    @DisplayName("no template still uses a placeholder no agent supplies")
    void noTemplateHasAnUnsuppliedPlaceholder() throws Exception {
        // Rendering with every known key at once must leave nothing behind.
        Map<String, Object> all = Map.of(
                "story", "S", "analysis", "A", "testCases", "T", "scripts", "SC",
                "context", "C", "workflowData", "W");

        List<String> offenders = new ArrayList<>();
        for (String t : List.of("QA_ANALYSIS", "TEST_CASE_GENERATION", "SCRIPT_GENERATION",
                "EXECUTION", "BUG_ANALYSIS", "REPORT_GENERATION", "LEARNING_ENGINE", "SELF_HEALING")) {
            String left = PromptTemplateEngine.findUnresolvedPlaceholders(
                    new PromptTemplateEngine().render(read(t), all));
            if (!left.isEmpty()) {
                offenders.add(t + " -> " + left);
            }
        }
        assertFalse(offenders.size() > 0, "templates reference placeholders nothing supplies: " + offenders);
    }
}
