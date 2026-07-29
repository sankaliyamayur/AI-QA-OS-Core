package com.aiqaos.testdata.orchestrator;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.testdata.security.MaskingEngine;
import com.aiqaos.testdata.security.MaskingProperties;
import com.aiqaos.testdata.security.PiiDetector;
import com.aiqaos.testdata.security.SecureData;
import com.aiqaos.testdata.synthetic.SyntheticGenerator;
import com.aiqaos.testdata.validation.DataValidator;
import org.junit.jupiter.api.Test;

/**
 * MOD-4: the generate→mask→validate spine end-to-end over WF-1's real generator — synthesised
 * fixtures come out with their PII masked and no raw PII surviving.
 */
class TestDataOrchestratorTest {

    private final PiiDetector detector = new PiiDetector();
    private final TestDataOrchestrator orchestrator = new TestDataOrchestrator(
            new SyntheticGenerator(), new MaskingEngine(new MaskingProperties(), detector));
    private final DataValidator validator = new DataValidator(detector);

    @Test
    void paymentMethodFixtureIsMaskedAndClean() {
        SecureData sd = orchestrator.generateMasked("PAYMENT_METHOD");

        assertThat(sd.getMaskedFields()).contains("cardNumber");
        assertThat(String.valueOf(sd.getData().get("cardNumber"))).isNotEqualTo("4111222233334444");
        assertThat(validator.isClean(sd)).as("no raw PII survives masking").isTrue();
    }

    @Test
    void userProfileFixtureHasEmailMaskedAndClean() {
        SecureData sd = orchestrator.generateMasked("USER_PROFILE");

        assertThat(sd.getMaskedFields()).contains("email");
        assertThat(validator.residualPii(sd)).isEmpty();
    }

    @Test
    void unmaskedRecordWouldFailValidation() {
        // Sanity: the validator actually has teeth — an unmasked fixture (empty classification) leaks PII.
        SecureData raw = orchestrator.generateMasked("USER_PROFILE", java.util.Map.of());
        assertThat(validator.isClean(raw)).isFalse();
    }
}
