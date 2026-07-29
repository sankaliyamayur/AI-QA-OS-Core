package com.aiqaos.testdata.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** MOD-4: unit tests for the masking engine — each strategy, record masking, free-text masking. */
class MaskingEngineTest {

    private final PiiDetector detector = new PiiDetector();

    private MaskingEngine engine(MaskingProperties props) {
        return new MaskingEngine(props, detector);
    }

    private MaskingProperties defaults() {
        return new MaskingProperties(); // default strategy PARTIAL
    }

    @Test
    void partialEmailKeepsShapeButHidesPii() {
        String masked = engine(defaults()).mask("alice@example.com", PiiType.EMAIL);
        assertThat(masked).startsWith("a").contains("@").endsWith(".com");
        assertThat(detector.containsPii(masked)).as("masked email is not re-detectable").isFalse();
    }

    @Test
    void partialCreditCardKeepsLastFour() {
        String masked = engine(defaults()).mask("4111222233334444", PiiType.CREDIT_CARD);
        assertThat(masked).endsWith("4444");
        assertThat(detector.typesIn(masked)).doesNotContain(PiiType.CREDIT_CARD);
    }

    @Test
    void redactStrategyReplacesEntirely() {
        MaskingProperties props = defaults();
        props.getStrategies().put("CREDIT_CARD", "REDACT");
        assertThat(engine(props).mask("4111222233334444", PiiType.CREDIT_CARD)).isEqualTo("[REDACTED]");
    }

    @Test
    void hashStrategyIsDeterministic() {
        MaskingProperties props = defaults();
        props.setDefaultStrategy("HASH");
        MaskingEngine engine = engine(props);
        String a = engine.mask("alice@example.com", PiiType.EMAIL);
        String b = engine.mask("alice@example.com", PiiType.EMAIL);
        assertThat(a).isEqualTo(b).startsWith("tok_"); // stable token → referential integrity
        assertThat(engine.mask("bob@example.com", PiiType.EMAIL)).isNotEqualTo(a);
    }

    @Test
    void fakeStrategyReturnsSyntheticValue() {
        MaskingProperties props = defaults();
        props.setDefaultStrategy("FAKE");
        assertThat(engine(props).mask("alice@example.com", PiiType.EMAIL)).isEqualTo("user@example.test");
    }

    @Test
    void maskTextScrubsAllDetectablePii() {
        String masked = engine(defaults())
                .maskText("contact alice@example.com or call 555-123-4567 now");
        assertThat(detector.containsPii(masked)).isFalse();
    }

    @Test
    void maskRecordMasksOnlyClassifiedFields() {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("cardNumber", "4111222233334444");
        record.put("note", "leave me alone");
        Map<String, PiiType> classification = Map.of("cardNumber", PiiType.CREDIT_CARD);

        SecureData sd = engine(defaults()).maskRecord(record, classification);

        assertThat(sd.getMaskedFields()).containsExactly("cardNumber");
        assertThat(sd.getData().get("note")).isEqualTo("leave me alone");
        assertThat(String.valueOf(sd.getData().get("cardNumber"))).endsWith("4444").isNotEqualTo("4111222233334444");
    }
}
