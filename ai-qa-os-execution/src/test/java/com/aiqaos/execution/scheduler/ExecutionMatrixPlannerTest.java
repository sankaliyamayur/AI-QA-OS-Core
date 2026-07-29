package com.aiqaos.execution.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.execution.engine.BrowserType;
import com.aiqaos.execution.engine.ExecutionConfiguration;
import java.util.List;
import org.junit.jupiter.api.Test;

/** WF-4: matrix expansion — browsers × shards, with back-compatible defaults. */
class ExecutionMatrixPlannerTest {

    private final ExecutionMatrixPlanner planner = new ExecutionMatrixPlanner();

    @Test
    void defaultConfigYieldsExactlyOneUnshardedPlan() {
        List<ShardPlan> plans = planner.plan(new ExecutionConfiguration());

        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getBrowser()).isEqualTo(BrowserType.CHROME);
        assertThat(plans.get(0).isSharded()).isFalse();
    }

    @Test
    void expandsBrowsersTimesShards() {
        ExecutionConfiguration config = new ExecutionConfiguration();
        config.setBrowsers(List.of(BrowserType.CHROME, BrowserType.FIREFOX));
        config.setShardCount(2);

        List<ShardPlan> plans = planner.plan(config);

        assertThat(plans).hasSize(4);
        assertThat(plans).extracting(ShardPlan::toString)
                .containsExactly(
                        "CHROME shard 1/2", "CHROME shard 2/2",
                        "FIREFOX shard 1/2", "FIREFOX shard 2/2");
    }

    @Test
    void unitConfigCarriesBrowserAndShard() {
        ExecutionConfiguration base = new ExecutionConfiguration();
        base.setShardCount(3);
        ExecutionConfiguration unit = planner.unitConfig(base, new ShardPlan(BrowserType.FIREFOX, 2, 3));

        assertThat(unit.getBrowser()).isEqualTo(BrowserType.FIREFOX);
        assertThat(unit.getShardIndex()).isEqualTo(2);
        assertThat(unit.getShardCount()).isEqualTo(3);
    }
}
