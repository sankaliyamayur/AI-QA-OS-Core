package com.aiqaos.testdata.validation;

import com.aiqaos.testdata.security.PiiDetector;
import com.aiqaos.testdata.security.PiiType;
import com.aiqaos.testdata.security.SecureData;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

/**
 * MOD-4: the compliance assertion for masked data. Re-scans a {@link SecureData} snapshot with the
 * shared {@link PiiDetector} and reports any raw PII that survived masking — the check that gives
 * "no PII leaves the boundary" teeth.
 */
@Component
public class DataValidator {

    private final PiiDetector detector;

    public DataValidator(PiiDetector detector) {
        this.detector = detector;
    }

    /** Any auto-detectable PII still present across the masked record's string values. */
    public Set<PiiType> residualPii(SecureData secureData) {
        Set<PiiType> residual = new TreeSet<>();
        if (secureData == null) {
            return residual;
        }
        for (Map.Entry<String, Object> e : secureData.getData().entrySet()) {
            if (e.getValue() != null) {
                residual.addAll(detector.typesIn(String.valueOf(e.getValue())));
            }
        }
        return residual;
    }

    /** True if the masked record contains no auto-detectable raw PII. */
    public boolean isClean(SecureData secureData) {
        return residualPii(secureData).isEmpty();
    }
}
