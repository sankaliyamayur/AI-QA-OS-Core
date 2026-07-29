package com.aiqaos.testdata.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** MOD-4: unit tests for PII detection (each type; clean text; timestamp not misread as a card). */
class PiiDetectorTest {

    private final PiiDetector detector = new PiiDetector();

    @Test
    void detectsEmail() {
        assertThat(detector.typesIn("write to alice@example.com today")).contains(PiiType.EMAIL);
    }

    @Test
    void detectsSsn() {
        assertThat(detector.typesIn("SSN 123-45-6789")).contains(PiiType.SSN);
    }

    @Test
    void detectsContiguousAndGroupedCreditCards() {
        assertThat(detector.typesIn("card 4111222233334444")).contains(PiiType.CREDIT_CARD);
        assertThat(detector.typesIn("card 4111 2222 3333 4444")).contains(PiiType.CREDIT_CARD);
    }

    @Test
    void detectsPhone() {
        assertThat(detector.typesIn("call 555-123-4567")).contains(PiiType.PHONE);
    }

    @Test
    void cleanTextHasNoPii() {
        assertThat(detector.containsPii("Navigate to the login page and verify the header")).isFalse();
    }

    @Test
    void thirteenDigitTimestampIsNotACreditCard() {
        // System.currentTimeMillis() is 13 digits; the card pattern requires 15-16.
        assertThat(detector.typesIn("timestamp 1690000000000")).doesNotContain(PiiType.CREDIT_CARD);
    }
}
