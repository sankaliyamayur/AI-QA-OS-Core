package com.aiqaos.testdata.security;

import java.util.Map;

/**
 * MOD-4: masks PII in test data. Three entry points: a single classified {@link #mask value},
 * free-text {@link #maskText auto-detection}, and a whole {@link #maskRecord record} against a field
 * classification. Implemented by {@link MaskingEngine}.
 */
public interface MaskingService {

    /** Mask a single value known to be of the given {@link PiiType}. */
    String mask(String value, PiiType type);

    /** Auto-detect PII in free text and mask each occurrence in place. */
    String maskText(String freeText);

    /**
     * Mask the classified fields of a record. {@code classification} maps field name → {@link PiiType};
     * fields absent from it are left untouched. Returns the masked data plus the set of masked fields.
     */
    SecureData maskRecord(Map<String, Object> record, Map<String, PiiType> classification);
}
