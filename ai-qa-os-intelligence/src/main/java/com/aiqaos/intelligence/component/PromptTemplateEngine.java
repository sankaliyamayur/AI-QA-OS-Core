package com.aiqaos.intelligence.component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Renders a prompt template by substituting {@code {{placeholder}}} values.
 *
 * <p><b>Whitespace tolerance is not cosmetic.</b> This used to do a literal
 * {@code replace("{{" + key + "}}", value)}, which matched {@code {{story}}} but not
 * {@code {{ story }}}. A template written with the spaced form silently kept its placeholder:
 * {@code String.replace} finds nothing and returns the input unchanged, with no error anywhere. The
 * effect was that agents sent the LLM a prompt reading "Requirement:\n{{ story }}" — instructions
 * with no content — and the model, given nothing to work from, invented plausible answers. A real
 * run generated a Playwright test against {@code example.com/login} with SauceDemo credentials while
 * the user story sitting on disk named the actual application, URL and login details.
 *
 * <p>Both spellings now resolve, and anything left unresolved is reported rather than shipped —
 * see {@link #findUnresolvedPlaceholders(String)}. The failure mode this class had was the worst
 * kind: it looked like success at every layer.
 */
@Component
public class PromptTemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateEngine.class);

    /** Any {@code {{ ... }}} token, so a leftover can be detected whatever its spacing. */
    private static final Pattern ANY_PLACEHOLDER = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.\\-]+)\\s*}}");

    public String render(String template, Map<String, Object> params) {
        if (template == null) {
            return "";
        }
        if (params == null) {
            return template;
        }

        String rendered = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String value = String.valueOf(entry.getValue());
            // Tolerates {{key}} and {{ key }} alike. quoteReplacement matters: rendered values are
            // JSON and user-story text, which routinely contain $ and \ — unescaped they would be
            // read as group references and corrupt the prompt.
            Pattern p = Pattern.compile("\\{\\{\\s*" + Pattern.quote(entry.getKey()) + "\\s*}}");
            rendered = p.matcher(rendered).replaceAll(Matcher.quoteReplacement(value));
        }

        // Loud, but not fatal: a half-rendered prompt still produces a plausible-looking answer, so
        // silence is what let this go unnoticed. Throwing here would take down callers that
        // legitimately render partial templates, so the contract is "always visible in the log",
        // and the assertion lives in the tests instead.
        String leftovers = findUnresolvedPlaceholders(rendered);
        if (!leftovers.isEmpty()) {
            log.error("Prompt template rendered with UNRESOLVED placeholders: {} — the model will be "
                    + "sent instructions with no content and will invent an answer. Supplied params: {}",
                    leftovers, params.keySet());
        }

        return rendered;
    }

    /** Comma-separated names of any placeholders still present, or empty when fully rendered. */
    public static String findUnresolvedPlaceholders(String rendered) {
        if (rendered == null || rendered.isEmpty()) {
            return "";
        }
        Matcher m = ANY_PLACEHOLDER.matcher(rendered);
        StringBuilder found = new StringBuilder();
        while (m.find()) {
            if (found.length() > 0) {
                found.append(", ");
            }
            found.append(m.group(1));
        }
        return found.toString();
    }
}
