package com.aiqaos.provider.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Every provider in the failover chain failed. The workflow cannot continue on real AI.
 *
 * <p><b>This must never be converted into a passing run.</b> The whole point of the failover work is
 * that a real-AI execution which could not reach any real provider is a failed execution, not a
 * green one — previously a provider failure fell back to the Simulator, which returned a canned
 * answer and the pipeline reported SUCCESS. Two rows in the live database showed the fingerprint of
 * that bug: {@code provider='Gemini'} recorded against {@code model='local-simulator-v1'}.
 *
 * <p>{@code AutonomousQAPipelineOrchestrator} therefore treats this as fatal even inside
 * {@code ExecutionStep}, which is otherwise allowed to fail and continue so that
 * {@code BugAnalysisStep} can diagnose a genuine test failure. A test failing is a result; having no
 * AI to run the test with is not.
 *
 * <p>The message lists every attempt so the cause is visible without trawling logs:
 * {@code openai: HTTP 429 ...; claude: HTTP 401 ...; gemini: timeout ...}
 */
public class AllProvidersExhaustedException extends ProviderException {

    /**
     * Stable sentinel embedded in every message.
     *
     * <p>Pipeline steps catch {@code Exception} broadly and collapse it into
     * {@code response.setStatus("FAILED")} with {@code e.getMessage()} appended, so by the time the
     * orchestrator sees a provider failure the exception type is gone and only the text survives.
     * The orchestrator matches on this marker to tell "the AI was unreachable" apart from "the test
     * under test failed" — a distinction it must make, because it is allowed to continue past the
     * second but never past the first.
     */
    public static final String MARKER = "LLM_PROVIDERS_EXHAUSTED";

    /** True if this failure, or anything it wrapped, was a provider exhaustion. */
    public static boolean isExhaustion(String message) {
        return message != null && message.contains(MARKER);
    }

    private final List<Attempt> attempts;

    public AllProvidersExhaustedException(List<Attempt> attempts) {
        super(buildMessage(attempts), firstCause(attempts), 0, false);
        this.attempts = List.copyOf(attempts);
    }

    private static String buildMessage(List<Attempt> attempts) {
        if (attempts == null || attempts.isEmpty()) {
            return MARKER + ": no LLM provider was available to serve the request. "
                    + "Configure at least one real provider key (OPENAI_API_KEY, ANTHROPIC_API_KEY, "
                    + "GEMINI_API_KEY) or enable Ollama.";
        }
        List<String> parts = new ArrayList<>();
        for (Attempt a : attempts) {
            parts.add(a.provider() + ": " + a.reason());
        }
        return MARKER + ": all " + attempts.size() + " LLM provider(s) failed — " + String.join("; ", parts);
    }

    private static Throwable firstCause(List<Attempt> attempts) {
        if (attempts == null) {
            return null;
        }
        return attempts.stream().map(Attempt::cause).filter(java.util.Objects::nonNull).findFirst().orElse(null);
    }

    public List<Attempt> getAttempts() {
        return attempts;
    }

    /** One provider's failure within the chain. */
    public record Attempt(String provider, int status, String reason, Throwable cause) {

        public static Attempt of(String provider, ProviderException e) {
            String reason = e.getStatus() > 0 ? "HTTP " + e.getStatus() + " " + e.getMessage() : e.getMessage();
            return new Attempt(provider, e.getStatus(), reason, e);
        }
    }
}
