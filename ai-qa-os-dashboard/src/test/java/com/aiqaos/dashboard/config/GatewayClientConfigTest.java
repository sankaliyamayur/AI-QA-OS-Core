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
import org.springframework.boot.restclient.RestTemplateBuilder;
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

    /**
     * ADR-093: without this subscription a rotated certificate would need a pod restart —
     * {@code reload-on-update} refreshes the bundle, but a client built from it has already captured
     * the old {@code SSLContext}.
     */
    @Test
    void theClientSubscribesToBundleUpdatesSoRotationDoesNotNeedARestart() {
        RecordingBundles bundles = new RecordingBundles(true);

        config.gatewayRestTemplate(new RestTemplateBuilder(), bundles, "gateway-client");

        assertEquals(List.of("gateway-client"), bundles.updateHandlersFor,
                "an update handler must be registered for the configured bundle");
    }

    @Test
    void firingABundleUpdateDoesNotDisturbTheInjectedClientReference() {
        RecordingBundles bundles = new RecordingBundles(true);

        RestTemplate client = config.gatewayRestTemplate(new RestTemplateBuilder(), bundles, "gateway-client");
        bundles.fireUpdate(SslBundle.of(null));

        // Callers (ReviewController) hold this reference for the life of the context, so a rotation
        // must swap the factory underneath rather than replace the RestTemplate.
        assertNotNull(client.getRequestFactory());
    }

    @Test
    void noBundleMeansNoUpdateSubscription() {
        RecordingBundles bundles = new RecordingBundles(false);

        config.gatewayRestTemplate(new RestTemplateBuilder(), bundles, "");

        assertTrue(bundles.updateHandlersFor.isEmpty());
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
        private final List<String> updateHandlersFor = new ArrayList<>();
        private final List<java.util.function.Consumer<SslBundle>> handlers = new ArrayList<>();
        private final boolean resolvable;

        RecordingBundles(boolean resolvable) {
            this.resolvable = resolvable;
        }

        /** Simulate the file watcher noticing a rotated keystore. */
        void fireUpdate(SslBundle updated) {
            handlers.forEach(h -> h.accept(updated));
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
            updateHandlersFor.add(name);
            handlers.add(updateHandler);
        }

        // Spring Boot 4 widened the SslBundles contract with these two. Neither is exercised here —
        // they exist so the stub still implements the interface.
        @Override
        public java.util.List<String> getBundleNames() {
            return List.of();
        }

        @Override
        public void addBundleRegisterHandler(java.util.function.BiConsumer<String, SslBundle> registerHandler) {
            // not exercised
        }
    }
}
