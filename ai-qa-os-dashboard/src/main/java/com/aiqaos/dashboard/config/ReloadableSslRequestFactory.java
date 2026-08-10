package com.aiqaos.dashboard.config;

import java.io.IOException;
import java.net.URI;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

/**
 * SEC-6 (ADR-093): a request factory whose TLS material can be swapped while the application runs.
 *
 * <p><b>Why this indirection is needed.</b> {@code spring.ssl.bundle…reload-on-update} refreshes the
 * <i>bundle</i> when its keystore changes on disk, and the embedded web server subscribes to that and
 * re-arms its listener. An HTTP <i>client</i> does not: {@code RestTemplateBuilder.setSslBundle(…)}
 * resolves the bundle once and bakes the resulting {@code SSLContext} into a request factory at build
 * time. Left alone, a dashboard that had been running since before a rotation would keep presenting
 * the retired certificate until the pod restarted — exactly the gap this closes.
 *
 * <p>The indirection keeps the injected {@code RestTemplate} reference stable — callers such as
 * {@code ReviewController} hold it for the life of the context — while the delegate underneath is
 * rebuilt on each bundle update. {@code delegate} is {@code volatile} because the file watcher runs
 * on its own thread and request threads must see the replacement without further synchronisation.
 *
 * <p><b>In-flight requests keep the old factory,</b> deliberately: a connection already being
 * established completes under the material it started with, and the next request picks up the new
 * one. Tearing down live connections mid-rotation would turn a certificate refresh into an outage.
 */
class ReloadableSslRequestFactory implements ClientHttpRequestFactory {

    private final ClientHttpRequestFactorySettings baseSettings;
    private volatile ClientHttpRequestFactory delegate;

    ReloadableSslRequestFactory(SslBundle bundle, ClientHttpRequestFactorySettings baseSettings) {
        this.baseSettings = baseSettings;
        this.delegate = build(bundle);
    }

    /** Swap in a factory built from the refreshed bundle. Called by the SSL bundle update handler. */
    void reload(SslBundle updated) {
        this.delegate = build(updated);
    }

    private ClientHttpRequestFactory build(SslBundle bundle) {
        return ClientHttpRequestFactories.get(baseSettings.withSslBundle(bundle));
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return delegate.createRequest(uri, httpMethod);
    }
}
