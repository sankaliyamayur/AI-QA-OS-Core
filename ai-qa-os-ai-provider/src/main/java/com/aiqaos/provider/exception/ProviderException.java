package com.aiqaos.provider.exception;

/**
 * A single provider's failure.
 *
 * <p>Carries the HTTP status where there was one, because failover has to tell two very different
 * situations apart: a 429 or a 503 is worth trying the next provider for, while a 400 means the
 * request itself is wrong and every other provider will reject it the same way — failing over there
 * just burns quota to arrive at the same error.
 *
 * <p>The status-free constructors keep existing call sites working; they classify as retryable,
 * which is the safe default for transport-level faults (timeouts, connection refused) that arrive
 * without a status.
 */
public class ProviderException extends RuntimeException {

    /** HTTP status, or 0 when the failure had none (timeout, connection refused, no key). */
    private final int status;

    private final boolean retryable;

    public ProviderException(String msg) {
        this(msg, null, 0, true);
    }

    public ProviderException(String msg, Throwable cause) {
        this(msg, cause, 0, true);
    }

    public ProviderException(String msg, Throwable cause, int status) {
        this(msg, cause, status, classify(status));
    }

    public ProviderException(String msg, Throwable cause, int status, boolean retryable) {
        super(msg, cause);
        this.status = status;
        this.retryable = retryable;
    }

    /**
     * Retryable on another provider: rate limits, server-side faults, and anything without a status
     * (timeouts and connection failures, which say nothing about the request's validity).
     *
     * <p>401/403 are retryable at this level on purpose. A provider only surfaces them after
     * {@code ApiKeyPool} has already rotated through every one of its own keys, so reaching here
     * means "this provider has no working credential" — a reason to try the next provider, not to
     * abandon the workflow.
     */
    private static boolean classify(int status) {
        if (status == 0) {
            return true;
        }
        if (status == 429 || status == 401 || status == 403) {
            return true;
        }
        return status >= 500 && status <= 599;
    }

    public int getStatus() {
        return status;
    }

    public boolean isRetryable() {
        return retryable;
    }

    /** A failure that will repeat identically on every provider — do not fail over. */
    public boolean isTerminal() {
        return !retryable;
    }
}
