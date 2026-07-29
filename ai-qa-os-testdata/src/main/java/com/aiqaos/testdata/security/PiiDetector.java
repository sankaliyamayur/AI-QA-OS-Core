package com.aiqaos.testdata.security;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * MOD-4: regex detection of PII in free text — shared by {@link MaskingEngine} (the auto-detect
 * masking path) and {@code DataValidator} (the residual-PII compliance check) so the patterns live
 * in one place. Detects {@code EMAIL}, {@code SSN}, {@code CREDIT_CARD}, {@code PHONE}; {@code NAME}
 * is intentionally excluded (not reliably regex-detectable).
 *
 * <p>The credit-card pattern requires 15–16 digits (contiguous or 4-grouped) so 13-digit epoch
 * timestamps are not misread as card numbers.
 */
@Component
public class PiiDetector {

    /** Ordered so longer / more specific matches (email, ssn, card) run before phone. */
    private static final Map<PiiType, Pattern> PATTERNS = new LinkedHashMap<>();

    static {
        PATTERNS.put(PiiType.EMAIL,
                Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"));
        PATTERNS.put(PiiType.SSN,
                Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"));
        PATTERNS.put(PiiType.CREDIT_CARD,
                Pattern.compile("\\b(?:\\d{4}[ -]\\d{4}[ -]\\d{4}[ -]\\d{3,4}|\\d{15,16})\\b"));
        PATTERNS.put(PiiType.PHONE,
                Pattern.compile("\\b(?:\\+?\\d{1,2}[ -]?)?\\(?\\d{3}\\)?[ -]\\d{3}[ -]\\d{4}\\b"));
    }

    static Map<PiiType, Pattern> patterns() {
        return PATTERNS;
    }

    /** The set of PII types present in {@code text} (empty if none / null). */
    public Set<PiiType> typesIn(String text) {
        Set<PiiType> found = new TreeSet<>();
        if (text == null || text.isEmpty()) {
            return found;
        }
        for (Map.Entry<PiiType, Pattern> e : PATTERNS.entrySet()) {
            if (e.getValue().matcher(text).find()) {
                found.add(e.getKey());
            }
        }
        return found;
    }

    /** Whether {@code text} contains any detectable PII. */
    public boolean containsPii(String text) {
        return !typesIn(text).isEmpty();
    }
}
