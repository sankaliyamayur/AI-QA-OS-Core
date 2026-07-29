package com.aiqaos.testdata.security;

/**
 * MOD-4: the kinds of personally-identifiable information the masking engine recognises. {@code EMAIL},
 * {@code CREDIT_CARD}, {@code SSN}, and {@code PHONE} are auto-detectable from free text; {@code NAME}
 * and {@code GENERIC} are only masked when a caller classifies a field as such (names can't be
 * regex-detected reliably).
 */
public enum PiiType {
    EMAIL,
    CREDIT_CARD,
    SSN,
    PHONE,
    NAME,
    GENERIC
}
