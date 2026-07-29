package com.aiqaos.provider.cost;

/**
 * ENT-3: the outcome of a pre-flight budget check — allowed, or which scope/limit was exceeded.
 */
public class BudgetVerdict {

    private final boolean allowed;
    private final String scope;
    private final Double limit;
    private final double spend;

    private BudgetVerdict(boolean allowed, String scope, Double limit, double spend) {
        this.allowed = allowed;
        this.scope = scope;
        this.limit = limit;
        this.spend = spend;
    }

    public static BudgetVerdict allow() {
        return new BudgetVerdict(true, null, null, 0.0);
    }

    public static BudgetVerdict exceeded(String scope, Double limit, double spend) {
        return new BudgetVerdict(false, scope, limit, spend);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getScope() {
        return scope;
    }

    public Double getLimit() {
        return limit;
    }

    public double getSpend() {
        return spend;
    }
}
