package com.aiqaos.testdata.orchestrator;

import com.aiqaos.testdata.security.MaskingService;
import com.aiqaos.testdata.security.PiiType;
import com.aiqaos.testdata.security.SecureData;
import com.aiqaos.testdata.synthetic.SyntheticGenerator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * MOD-4: the generate→mask spine. Drives WF-1's real {@link SyntheticGenerator} to produce a fixture,
 * then masks its PII fields via the {@link MaskingService} — proving the module works end-to-end and
 * that synthesised data can be handed on with no raw PII. Known dataset types carry a default PII
 * classification; callers can also supply their own.
 */
@Component
public class TestDataOrchestrator {

    private final SyntheticGenerator generator;
    private final MaskingService maskingService;

    public TestDataOrchestrator(SyntheticGenerator generator, MaskingService maskingService) {
        this.generator = generator;
        this.maskingService = maskingService;
    }

    /** Generate a fixture of {@code datasetType} and mask its PII using the default classification. */
    public SecureData generateMasked(String datasetType) {
        return generateMasked(datasetType, defaultClassification(datasetType));
    }

    /** Generate a fixture of {@code datasetType} and mask the fields named in {@code classification}. */
    public SecureData generateMasked(String datasetType, Map<String, PiiType> classification) {
        Map<String, Object> fixture = generator.generateFixture(datasetType);
        return maskingService.maskRecord(fixture, classification);
    }

    /** The PII fields the WF-1 generator is known to emit, per dataset type. */
    public Map<String, PiiType> defaultClassification(String datasetType) {
        Map<String, PiiType> c = new LinkedHashMap<>();
        String type = datasetType != null ? datasetType.toUpperCase() : "USER_PROFILE";
        switch (type) {
            case "PAYMENT_METHOD":
                c.put("cardNumber", PiiType.CREDIT_CARD);
                c.put("cvv", PiiType.GENERIC);
                c.put("billingZip", PiiType.GENERIC);
                break;
            case "ORDER_ITEM":
                // no PII fields
                break;
            default: // USER_PROFILE
                c.put("email", PiiType.EMAIL);
                c.put("username", PiiType.GENERIC);
                break;
        }
        return c;
    }
}
