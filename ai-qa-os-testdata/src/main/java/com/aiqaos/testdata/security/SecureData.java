package com.aiqaos.testdata.security;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * MOD-4: the result of masking a record — the masked data plus which fields were masked. Immutable
 * snapshot returned by {@link MaskingService#maskRecord}.
 */
public final class SecureData {

    private final Map<String, Object> data;
    private final Set<String> maskedFields;

    public SecureData(Map<String, Object> data, Set<String> maskedFields) {
        this.data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
        this.maskedFields = Collections.unmodifiableSet(new LinkedHashSet<>(maskedFields));
    }

    /** The masked record. */
    public Map<String, Object> getData() { return data; }

    /** Names of the fields that were masked. */
    public Set<String> getMaskedFields() { return maskedFields; }

    public int getMaskedCount() { return maskedFields.size(); }

    @Override
    public String toString() {
        return "SecureData{maskedFields=" + maskedFields + ", fields=" + data.keySet() + "}";
    }
}
