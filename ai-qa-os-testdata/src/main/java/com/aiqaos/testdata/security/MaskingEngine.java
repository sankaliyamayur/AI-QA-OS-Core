package com.aiqaos.testdata.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * MOD-4: the PII masking engine (the module's flagship compliance feature). Applies the
 * {@link MaskingStrategy} configured per {@link PiiType} ({@code PARTIAL} by default) — format-preserving
 * partial masks, deterministic {@code HASH} pseudonymisation, {@code REDACT}, or {@code FAKE}. Pure and
 * deterministic: no I/O, no live services.
 */
@Component
public class MaskingEngine implements MaskingService {

    private static final String REDACTED = "[REDACTED]";

    private final MaskingProperties properties;
    private final PiiDetector detector;

    public MaskingEngine(MaskingProperties properties, PiiDetector detector) {
        this.properties = properties;
        this.detector = detector;
    }

    @Override
    public String mask(String value, PiiType type) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        MaskingStrategy strategy = properties.strategyFor(type);
        switch (strategy) {
            case REDACT:
                return REDACTED;
            case HASH:
                return "tok_" + sha256(properties.getHashSalt() + value);
            case FAKE:
                return fake(type);
            case PARTIAL:
            default:
                return partial(value, type);
        }
    }

    @Override
    public String maskText(String freeText) {
        if (freeText == null || freeText.isEmpty()) {
            return freeText;
        }
        String result = freeText;
        for (Map.Entry<PiiType, Pattern> e : PiiDetector.patterns().entrySet()) {
            PiiType type = e.getKey();
            Matcher m = e.getValue().matcher(result);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                m.appendReplacement(sb, Matcher.quoteReplacement(mask(m.group(), type)));
            }
            m.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }

    @Override
    public SecureData maskRecord(Map<String, Object> record, Map<String, PiiType> classification) {
        Map<String, Object> masked = new LinkedHashMap<>(record);
        Set<String> maskedFields = new LinkedHashSet<>();
        if (classification != null) {
            for (Map.Entry<String, PiiType> c : classification.entrySet()) {
                String field = c.getKey();
                if (masked.containsKey(field) && masked.get(field) != null) {
                    masked.put(field, mask(String.valueOf(masked.get(field)), c.getValue()));
                    maskedFields.add(field);
                }
            }
        }
        return new SecureData(masked, maskedFields);
    }

    // --- PARTIAL, format-preserving per type -------------------------------------------------

    private String partial(String value, PiiType type) {
        switch (type) {
            case EMAIL:
                return partialEmail(value);
            case CREDIT_CARD:
            case SSN:
            case PHONE:
                return keepLast(digitsOrRaw(value), 4);
            case NAME:
            case GENERIC:
            default:
                return keepFirst(value, 1);
        }
    }

    private String partialEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0 || at == email.length() - 1) {
            return keepFirst(email, 1);
        }
        String local = email.substring(0, at);
        String domain = email.substring(at + 1);
        int dot = domain.lastIndexOf('.');
        String host = dot > 0 ? domain.substring(0, dot) : domain;
        String tld = dot > 0 ? domain.substring(dot) : "";
        return keepFirst(local, 1) + "@" + keepFirst(host, 1) + tld;
    }

    /** Keep the first {@code n} chars, mask the rest (never fewer than one mask char). */
    private String keepFirst(String value, int n) {
        if (value.length() <= n) {
            return repeat(properties.getMaskChar(), Math.max(1, value.length()));
        }
        return value.substring(0, n) + repeat(properties.getMaskChar(), value.length() - n);
    }

    /** Keep the last {@code n} chars, mask the rest. */
    private String keepLast(String value, int n) {
        if (value.length() <= n) {
            return repeat(properties.getMaskChar(), Math.max(1, value.length()));
        }
        return repeat(properties.getMaskChar(), value.length() - n) + value.substring(value.length() - n);
    }

    private String digitsOrRaw(String value) {
        String digits = value.replaceAll("\\D", "");
        return digits.isEmpty() ? value : digits;
    }

    private String repeat(String ch, int times) {
        String c = (ch == null || ch.isEmpty()) ? "*" : ch;
        return c.repeat(Math.max(0, times));
    }

    // --- FAKE ---------------------------------------------------------------------------------

    private String fake(PiiType type) {
        switch (type) {
            case EMAIL:       return "user@example.test";
            case CREDIT_CARD: return "4000000000000000";
            case SSN:         return "000-00-0000";
            case PHONE:       return "000-000-0000";
            case NAME:        return "Test User";
            case GENERIC:
            default:          return "sample";
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6 && i < hash.length; i++) { // 12 hex chars — enough for a stable token
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
