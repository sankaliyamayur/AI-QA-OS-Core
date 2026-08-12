package com.aiqaos.provider.manager;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.exception.AllProvidersExhaustedException;
import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Walks the provider chain until one succeeds.
 *
 * <p>This used to take a single fallback — {@code try primary, else fallback, else give up} — which
 * could not express {@code OpenAI → Claude → Gemini}, and whose fallback slot was filled with the
 * Simulator. A rate-limited OpenAI therefore produced a canned Simulator answer that the pipeline
 * reported as success. The chain replaces both problems: any number of providers, and membership
 * decided by {@link com.aiqaos.provider.router.ModelRouter} rather than hardcoded here.
 *
 * <p>Key rotation stays <i>inside</i> each provider, below this layer, so every key for a provider
 * is spent before the chain advances — the ordering you want when one provider holds several keys.
 *
 * <p>Failing over is not always right. A 400 means the request is malformed and every provider will
 * say the same, so {@link ProviderException#isTerminal()} short-circuits the chain rather than
 * burning quota to reach the same error.
 */
@Component
public class LLMResilienceManager {

    private static final Logger log = LoggerFactory.getLogger(LLMResilienceManager.class);

    /** Result of a chain run: the response plus which provider actually produced it. */
    public record ChainResult(LLMResponse response, LLMProvider servingProvider) {
    }

    /**
     * @deprecated superseded by {@link #executeChain(List, LLMRequest, boolean, long)}. Retained so
     *     existing two-provider call sites and their tests keep working; it simply runs a chain of
     *     two.
     */
    @Deprecated
    public LLMResponse executeWithFallback(LLMProvider primary, LLMProvider fallback, LLMRequest request) {
        List<LLMProvider> chain = new ArrayList<>();
        chain.add(primary);
        if (fallback != null && fallback != primary) {
            chain.add(fallback);
        }
        return executeChain(chain, request, false, 0L).response();
    }

    public ChainResult executeChain(List<LLMProvider> chain, LLMRequest request) {
        return executeChain(chain, request, true, 1000L);
    }

    /**
     * Try each provider in order and return the first success.
     *
     * @throws AllProvidersExhaustedException when every provider failed, or the chain was empty
     * @throws ProviderException immediately on a terminal (non-retryable) failure
     */
    public ChainResult executeChain(List<LLMProvider> chain, LLMRequest request,
                                    boolean retryOnRateLimit, long retryBackoffMillis) {
        List<AllProvidersExhaustedException.Attempt> attempts = new ArrayList<>();

        if (chain == null || chain.isEmpty()) {
            throw new AllProvidersExhaustedException(attempts);
        }

        for (LLMProvider provider : chain) {
            String name = provider.getProviderName();
            try {
                return new ChainResult(provider.generate(request), provider);
            } catch (ProviderException e) {
                if (e.isTerminal()) {
                    // Same request, same rejection everywhere — do not spend the rest of the chain.
                    log.error("Provider {} rejected the request terminally (HTTP {}): {}",
                            name, e.getStatus(), e.getMessage());
                    throw e;
                }
                attempts.add(AllProvidersExhaustedException.Attempt.of(name, e));
                log.warn("Provider {} failed (HTTP {}): {} — advancing chain", name, e.getStatus(), e.getMessage());

                if (retryOnRateLimit && e.getStatus() == 429) {
                    ChainResult retried = retryOnce(provider, request, retryBackoffMillis, attempts);
                    if (retried != null) {
                        return retried;
                    }
                }
            } catch (Exception e) {
                // A provider that throws something other than ProviderException is still just a
                // failed provider; the chain must survive it.
                attempts.add(new AllProvidersExhaustedException.Attempt(name, 0, e.toString(), e));
                log.warn("Provider {} failed with an unexpected error: {} — advancing chain", name, e.toString(), e);
            }
        }

        AllProvidersExhaustedException exhausted = new AllProvidersExhaustedException(attempts);
        log.error("LLM provider chain exhausted: {}", exhausted.getMessage());
        throw exhausted;
    }

    /** One backoff-and-retry against the same provider; null if it failed again. */
    private ChainResult retryOnce(LLMProvider provider, LLMRequest request, long backoffMillis,
                                  List<AllProvidersExhaustedException.Attempt> attempts) {
        try {
            if (backoffMillis > 0) {
                Thread.sleep(backoffMillis);
            }
            log.info("Retrying {} once after rate limit", provider.getProviderName());
            return new ChainResult(provider.generate(request), provider);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ProviderException e) {
            attempts.add(AllProvidersExhaustedException.Attempt.of(provider.getProviderName() + " (retry)", e));
            return null;
        } catch (Exception e) {
            attempts.add(new AllProvidersExhaustedException.Attempt(
                    provider.getProviderName() + " (retry)", 0, e.toString(), e));
            return null;
        }
    }
}
