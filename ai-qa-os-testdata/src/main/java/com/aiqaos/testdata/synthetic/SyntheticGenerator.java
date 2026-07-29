package com.aiqaos.testdata.synthetic;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * WF-1 + TEST-DATA: Synthetic Data Generator.
 * Produces synthetic test data fixtures for workflow execution modes.
 */
@Component
public class SyntheticGenerator {

    public Map<String, Object> generateFixture(String datasetType) {
        Map<String, Object> fixture = new HashMap<>();
        String type = datasetType != null ? datasetType.toUpperCase() : "USER_PROFILE";
        fixture.put("fixtureId", "fix-" + UUID.randomUUID().toString().substring(0, 8));
        fixture.put("datasetType", type);
        fixture.put("timestamp", System.currentTimeMillis());

        switch (type) {
            case "PAYMENT_METHOD":
                fixture.put("cardNumber", "4111222233334444");
                fixture.put("expiry", "12/28");
                fixture.put("cvv", "999");
                fixture.put("billingZip", "90210");
                break;
            case "ORDER_ITEM":
                fixture.put("sku", "SKU-QAOS-" + System.currentTimeMillis() % 1000);
                fixture.put("quantity", 2);
                fixture.put("unitPrice", 49.99);
                break;
            default: // USER_PROFILE
                fixture.put("username", "qa_user_" + System.currentTimeMillis() % 10000);
                fixture.put("email", "qa_user_" + System.currentTimeMillis() % 10000 + "@aiqaos.local");
                fixture.put("role", "ROLE_QA_TESTER");
                break;
        }

        return fixture;
    }
}