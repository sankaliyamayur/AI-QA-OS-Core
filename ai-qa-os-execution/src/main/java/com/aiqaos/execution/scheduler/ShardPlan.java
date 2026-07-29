package com.aiqaos.execution.scheduler;

import com.aiqaos.execution.engine.BrowserType;

/**
 * WF-4: one unit of the execution matrix — a single browser running a single Playwright shard.
 * A run's matrix is every {@code browser × shardIndex} produced by {@link ExecutionMatrixPlanner}.
 */
public class ShardPlan {

    private final BrowserType browser;
    private final int shardIndex;
    private final int shardCount;

    public ShardPlan(BrowserType browser, int shardIndex, int shardCount) {
        this.browser = browser;
        this.shardIndex = shardIndex;
        this.shardCount = shardCount;
    }

    public BrowserType getBrowser() {
        return browser;
    }

    public int getShardIndex() {
        return shardIndex;
    }

    public int getShardCount() {
        return shardCount;
    }

    /** True when the suite is split into more than one shard (so Playwright needs {@code --shard}). */
    public boolean isSharded() {
        return shardCount > 1;
    }

    @Override
    public String toString() {
        return browser + (isSharded() ? " shard " + shardIndex + "/" + shardCount : "");
    }
}
