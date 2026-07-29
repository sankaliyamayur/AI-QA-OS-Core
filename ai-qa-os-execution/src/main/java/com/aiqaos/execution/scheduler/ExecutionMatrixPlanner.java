package com.aiqaos.execution.scheduler;

import com.aiqaos.execution.engine.BrowserType;
import com.aiqaos.execution.engine.ExecutionConfiguration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * WF-4: expands an {@link ExecutionConfiguration} into the execution matrix — one {@link ShardPlan}
 * per {@code browser × shard}. Empty browser matrix falls back to the single {@code browser}, and
 * {@code shardCount <= 1} means no sharding, so the pre-WF-4 default yields exactly one plan.
 */
@Component
public class ExecutionMatrixPlanner {

    public List<ShardPlan> plan(ExecutionConfiguration config) {
        List<BrowserType> browsers = (config.getBrowsers() == null || config.getBrowsers().isEmpty())
                ? List.of(config.getBrowser())
                : config.getBrowsers();
        int shards = Math.max(1, config.getShardCount());

        List<ShardPlan> plans = new ArrayList<>();
        for (BrowserType browser : browsers) {
            for (int shardIndex = 1; shardIndex <= shards; shardIndex++) {
                plans.add(new ShardPlan(browser, shardIndex, shards));
            }
        }
        return plans;
    }

    /** A per-unit configuration derived from {@code base}: the plan's browser + shard, other fields copied. */
    public ExecutionConfiguration unitConfig(ExecutionConfiguration base, ShardPlan plan) {
        ExecutionConfiguration unit = new ExecutionConfiguration();
        unit.setExecutionMode(base.getExecutionMode());
        unit.setEnvironment(base.getEnvironment());
        unit.setTimeout(base.getTimeout());
        unit.setRetryCount(base.getRetryCount());
        unit.setHeadless(base.isHeadless());
        unit.setBrowser(plan.getBrowser());
        unit.setShardCount(plan.getShardCount());
        unit.setShardIndex(plan.isSharded() ? plan.getShardIndex() : 0);
        return unit;
    }
}
