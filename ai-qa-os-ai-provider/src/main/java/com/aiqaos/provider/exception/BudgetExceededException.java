package com.aiqaos.provider.exception;

/**
 * ENT-3: thrown when an LLM call is refused because its cost-quota scope (global / per-workflow /
 * per-agent) has hit its configured limit and enforcement mode is {@code enforce}.
 */
public class BudgetExceededException extends ProviderException {

    public BudgetExceededException(String message) {
        super(message);
    }
}
