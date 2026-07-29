package com.aiqaos.healing.locator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * HEAL-1: deterministic locator healer. From the target element's known attributes it emits
 * candidate locators, preferring the most robust strategy available ({@code data-testid} → {@code id}
 * → {@code name} → {@code role} → {@code text} → {@code css}); if only the broken locator is known it
 * offers a relaxed fallback (e.g. an absolute xpath with its positional index stripped). Candidates
 * are de-duplicated and returned best-first by robustness confidence. Pure — no I/O, no LLM.
 */
@Component
public class HeuristicLocatorHealer implements LocatorHealer {

    @Override
    public List<LocatorCandidate> propose(LocatorHealingRequest request) {
        List<LocatorCandidate> candidates = new ArrayList<>();
        if (request == null) {
            return candidates;
        }

        Map<String, String> attrs = request.getAttributes();
        addAttributeCandidates(candidates, attrs);

        // Fallback: derive a relaxed candidate from the broken locator itself.
        LocatorCandidate relaxed = relaxedFromBroken(request.getBrokenLocator());
        if (relaxed != null) {
            candidates.add(relaxed);
        }

        return dedupeAndRank(candidates);
    }

    private void addAttributeCandidates(List<LocatorCandidate> out, Map<String, String> attrs) {
        String testId = firstNonBlank(attrs.get("data-testid"), attrs.get("data-test-id"),
                attrs.get("data-test"));
        if (testId != null) {
            out.add(candidate(LocatorStrategy.TEST_ID, "[data-testid=\"" + testId + "\"]",
                    "stable test id"));
        }
        String id = attrs.get("id");
        if (isPresent(id)) {
            out.add(candidate(LocatorStrategy.ID, "#" + id, "element id"));
        }
        String name = attrs.get("name");
        if (isPresent(name)) {
            out.add(candidate(LocatorStrategy.NAME, "[name=\"" + name + "\"]", "name attribute"));
        }
        String role = attrs.get("role");
        if (isPresent(role)) {
            out.add(candidate(LocatorStrategy.ROLE, "[role=\"" + role + "\"]", "aria role"));
        }
        String text = firstNonBlank(attrs.get("text"), attrs.get("aria-label"));
        if (text != null) {
            out.add(candidate(LocatorStrategy.TEXT, "text=\"" + text + "\"", "visible text / label"));
        }
        String cssClass = attrs.get("class");
        if (isPresent(cssClass)) {
            out.add(candidate(LocatorStrategy.CSS, cssSelectorFromClass(cssClass), "class selector"));
        }
    }

    private LocatorCandidate relaxedFromBroken(String broken) {
        if (!isPresent(broken)) {
            return null;
        }
        String trimmed = broken.trim();
        if (trimmed.startsWith("//") || trimmed.startsWith("(")) {
            // Strip a trailing positional predicate like [3] to make an absolute xpath less brittle.
            String relaxed = trimmed.replaceAll("\\[\\d+\\]", "");
            return candidate(LocatorStrategy.XPATH, relaxed, "relaxed xpath (positional index removed)");
        }
        // Unknown format — offer it back as a low-confidence CSS-ish fallback.
        return candidate(LocatorStrategy.XPATH, trimmed, "original locator (no better anchor available)");
    }

    private String cssSelectorFromClass(String classAttr) {
        StringBuilder sb = new StringBuilder();
        for (String token : classAttr.trim().split("\\s+")) {
            if (!token.isEmpty()) {
                sb.append('.').append(token);
            }
        }
        return sb.length() > 0 ? sb.toString() : classAttr;
    }

    private List<LocatorCandidate> dedupeAndRank(List<LocatorCandidate> candidates) {
        Set<String> seen = new LinkedHashSet<>();
        List<LocatorCandidate> unique = new ArrayList<>();
        for (LocatorCandidate c : candidates) {
            if (seen.add(c.getStrategy() + "|" + c.getValue())) {
                unique.add(c);
            }
        }
        // Stable sort, highest confidence first.
        unique.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
        return unique;
    }

    private LocatorCandidate candidate(LocatorStrategy strategy, String value, String rationale) {
        return new LocatorCandidate(value, strategy, strategy.baseConfidence(), rationale);
    }

    private static boolean isPresent(String s) {
        return s != null && !s.isBlank();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
