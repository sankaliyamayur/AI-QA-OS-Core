package com.aiqaos.intelligence.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Placeholders must actually be substituted, and a leftover must be detectable.
 *
 * <p><b>The bug this pins down.</b> {@code render} did a literal
 * {@code replace("{{" + key + "}}", value)}, so {@code {{story}}} substituted but {@code {{ story }}}
 * did not — {@code String.replace} simply finds nothing and returns the input, reporting no error.
 * Two prompt template sets existed at the same classpath path {@code /prompts/*.md}; the copy that
 * won the classpath used the spaced form, so **every** agent sent the model a prompt containing a
 * raw placeholder instead of content.
 *
 * <p>Observed consequence, from a real run: the model was asked to write a Playwright test from
 * "Test Cases:\n{{ testCases }}" and, having nothing to work from, invented one against
 * {@code example.com/login} with SauceDemo credentials — while the user story on disk named the
 * actual application, its URL and its login details. Nothing failed; every step reported SUCCESS.
 */
class PromptPlaceholderSubstitutionTest {

    private final PromptTemplateEngine engine = new PromptTemplateEngine();

    private static Map<String, Object> params(String key, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(key, value);
        return m;
    }

    // ── the two placeholders that actually broke ──────────────────────────────────────────────────

    @Test
    @DisplayName("{{ story }} — the spaced form that silently failed — is substituted")
    void spacedStoryPlaceholderIsSubstituted() {
        String rendered = engine.render("Requirement:\n{{ story }}\nAnalyse it.",
                params("story", "Admin URL: https://marketplace-admin.appworkdemo.com/"));

        assertTrue(rendered.contains("marketplace-admin.appworkdemo.com"), rendered);
        assertFalse(rendered.contains("{{"), "no placeholder may survive: " + rendered);
    }

    @Test
    @DisplayName("{{ testCases }} — spaced — is substituted")
    void spacedTestCasesPlaceholderIsSubstituted() {
        String rendered = engine.render("Test Cases:\n{{ testCases }}",
                params("testCases", "{\"id\":\"TC-AL-001\"}"));

        assertTrue(rendered.contains("TC-AL-001"), rendered);
        assertFalse(rendered.contains("{{"), rendered);
    }

    @Test
    @DisplayName("the unspaced form keeps working — MOD-3's prompts use it")
    void unspacedPlaceholderStillWorks() {
        assertEquals("Test Cases:\nDATA",
                engine.render("Test Cases:\n{{testCases}}", params("testCases", "DATA")));
    }

    @Test
    void irregularInnerWhitespaceIsAlsoHandled() {
        assertEquals("x=V", engine.render("x={{   story   }}", params("story", "V")));
        assertEquals("x=V", engine.render("x={{story }}", params("story", "V")));
        assertEquals("x=V", engine.render("x={{ story}}", params("story", "V")));
    }

    // ── detection of what did not resolve ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("an unresolved placeholder is detectable rather than shipped silently")
    void unresolvedPlaceholdersAreReported() {
        String rendered = engine.render("A={{ story }} B={{ missing }}", params("story", "S"));

        assertEquals("missing", PromptTemplateEngine.findUnresolvedPlaceholders(rendered));
        assertTrue(rendered.contains("A=S"), rendered);
    }

    @Test
    void aFullyRenderedPromptReportsNothingOutstanding() {
        String rendered = engine.render("A={{ story }}", params("story", "S"));
        assertEquals("", PromptTemplateEngine.findUnresolvedPlaceholders(rendered));
    }

    @Test
    @DisplayName("detection catches both spellings, so a spaced leftover cannot hide")
    void detectionIsSpacingAgnostic() {
        assertEquals("story", PromptTemplateEngine.findUnresolvedPlaceholders("x {{story}}"));
        assertEquals("story", PromptTemplateEngine.findUnresolvedPlaceholders("x {{ story }}"));
        assertEquals("", PromptTemplateEngine.findUnresolvedPlaceholders("no placeholders here"));
    }

    // ── values that would otherwise corrupt the prompt ────────────────────────────────────────────

    @Test
    @DisplayName("$ and backslash in the value are inserted literally, not read as group references")
    void regexMetacharactersInValuesSurviveIntact() {
        // The value is JSON, so the path is already backslash-escaped: the string really does
        // contain two backslashes, and the assertion has to look for two.
        String json = "{\"cost\":\"$100\",\"path\":\"C:\\\\tmp\",\"re\":\"$1\"}";

        String rendered = engine.render("Data:\n{{ testCases }}", params("testCases", json));

        assertTrue(rendered.contains("$100"), rendered);
        assertTrue(rendered.contains("C:\\\\tmp"), rendered);
        assertTrue(rendered.contains("$1"), rendered);
        assertEquals("Data:\n" + json, rendered, "the value must arrive byte-for-byte");
    }

    @Test
    void nullTemplateAndNullParamsBehaveAsBefore() {
        assertEquals("", engine.render(null, params("a", "b")));
        assertEquals("{{ story }}", engine.render("{{ story }}", null));
    }
}
