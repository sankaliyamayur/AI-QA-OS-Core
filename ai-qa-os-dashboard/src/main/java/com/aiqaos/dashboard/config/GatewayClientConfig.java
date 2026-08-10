package com.aiqaos.dashboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * SEC-6 / FI-SEC6-B: the client half of service-to-service mTLS.
 *
 * <p>The dashboard's AI-2 approve/reject proxy is the only place this platform calls another of its
 * own services over HTTP, so this one client is platform mTLS's client side. It used to be a
 * {@code new RestTemplate()} constructed inside the controller, which can never present a client
 * certificate no matter how the runtime is configured — Spring's SSL bundles only reach clients that
 * are <i>built</i> from them. That is why FI-SEC6-B could not be config-only, contrary to ADR-076's
 * framing of it as pure IaC.
 *
 * <p><b>Opt-in.</b> With {@code aiqaos.gateway.ssl-bundle} unset (the default) this produces exactly
 * the plaintext client the controller had before, so nothing changes for deployments not running
 * mTLS. Setting it to a bundle name — see {@code application-mtls.yml} — switches the same call onto
 * a certificate-bearing client.
 *
 * <p><b>A missing bundle is fatal, deliberately.</b> If a bundle is named but cannot be resolved we
 * fail startup rather than quietly falling back to a plaintext client: silently downgrading a
 * security control is worse than not starting, because the deployment would look mTLS-protected
 * while sending unauthenticated requests.
 */
@Configuration
public class GatewayClientConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayClientConfig.class);

    @Bean
    @ConditionalOnMissingBean(name = "gatewayRestTemplate")
    public RestTemplate gatewayRestTemplate(RestTemplateBuilder builder, SslBundles sslBundles,
                                            @Value("${aiqaos.gateway.ssl-bundle:}") String bundleName) {
        if (bundleName == null || bundleName.isBlank()) {
            return builder.build();
        }
        try {
            // ADR-093: route through a factory that can swap its TLS material, then subscribe to the
            // bundle. `reload-on-update` refreshes the bundle when the mounted keystore changes, but
            // a client built with setSslBundle(...) would have already baked in the old SSLContext —
            // so without this handler a rotated certificate needs a pod restart.
            SslBundle bundle = sslBundles.getBundle(bundleName);
            ReloadableSslRequestFactory factory =
                    new ReloadableSslRequestFactory(bundle, ClientHttpRequestFactorySettings.DEFAULTS);
            sslBundles.addBundleUpdateHandler(bundleName, updated -> {
                log.info("[mtls] SSL bundle '{}' updated — rebuilding the gateway client", bundleName);
                factory.reload(updated);
            });
            return builder.requestFactory(() -> factory).build();
        } catch (NoSuchSslBundleException e) {
            throw new IllegalStateException(
                    "aiqaos.gateway.ssl-bundle=" + bundleName + " is configured but no such SSL bundle "
                            + "exists — refusing to start with an unprotected gateway client (SEC-6). "
                            + "Define spring.ssl.bundle.jks." + bundleName + " or unset the property.", e);
        }
    }
}
