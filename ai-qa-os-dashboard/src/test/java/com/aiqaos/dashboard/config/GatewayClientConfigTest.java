package com.aiqaos.dashboard.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

/**
 * SEC-6 (FI-SEC6-B): the gateway client must only be built from an SSL bundle when one is named, and
 * must refuse to start rather than silently downgrade when a named bundle is missing.
 */
class GatewayClientConfigTest {

    private final GatewayClientConfig config = new GatewayClientConfig();

    @Test
    void noBundleConfiguredYieldsAPlainClientSoExistingDeploymentsAreUnaffected() {
        RecordingBundles bundles = new RecordingBundles(false);

        RestTemplate client = config.gatewayRestTemplate(new RestTemplateBuilder(), bundles, "");

        assertNotNull(client);
        assertTrue(bundles.requested.isEmpty(), "must not touch SSL bundles when none is configured");
    }

    @Test
    void nullBundleNameIsTreatedAsUnset() {
        RecordingBundles bundles = new RecordingBundles(false);

        assertNotNull(config.gatewayRestTemplate(new RestTemplateBuilder(), bundles, null));
        assertTrue(bundles.requested.isEmpty());
    }

    @Test
    void aNamedBundleIsResolvedAndAppliedToTheClient() {
        RecordingBundles bundles = new RecordingBundles(true);

        RestTemplate client = config.gatewayRestTemplate(new RestTemplateBuilder(), bundles, "gateway-client");

        assertNotNull(client);
        assertEquals(List.of("gateway-client"), bundles.requested,
                "the configured bundle must be the one applied — this is what carries the client cert");
    }

    @Test
    void aMissingBundleFailsFastRatherThanFallingBackToPlaintext() {
        RecordingBundles bundles = new RecordingBundles(false);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> config.gatewayRestTemplate(new RestTemplateBuilder(), bundles, "absent-bundle"));

        assertTrue(e.getMessage().contains("absent-bundle"));
        assertTrue(e.getMessage().contains("refusing to start"),
                "a silent downgrade would leave the deployment looking protected while it is not");
    }

    /** Records which bundles were asked for; {@code resolvable} decides whether lookup succeeds. */
    private static final class RecordingBundles implements SslBundles {

        private final List<String> requested = new ArrayList<>();
        private final boolean resolvable;

        RecordingBundles(boolean resolvable) {
            this.resolvable = resolvable;
        }

        @Override
        public SslBundle getBundle(String name) throws NoSuchSslBundleException {
            requested.add(name);
            if (!resolvable) {
                throw new NoSuchSslBundleException(name, "no such bundle");
            }
            return SslBundle.of(null);
        }

        @Override
        public void addBundleUpdateHandler(String name, java.util.function.Consumer<SslBundle> updateHandler) {
            // not exercised
        }
    }
}
