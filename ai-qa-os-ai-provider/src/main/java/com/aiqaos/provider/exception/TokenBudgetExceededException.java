package com.aiqaos.provider.exception;

/**
 * AI-6 (ADR-075): thrown when an LLM call is refused because its token/context-budget scope
 * (global / per-workflow / per-agent) has hit its configured token limit and enforcement mode is
 * {@code enforce}. The token counterpart to {@link BudgetExceededException}.
 */
public class TokenBudgetExceededException extends ProviderException {

    public TokenBudgetExceededException(String message) {
        super(message);
    }
}
