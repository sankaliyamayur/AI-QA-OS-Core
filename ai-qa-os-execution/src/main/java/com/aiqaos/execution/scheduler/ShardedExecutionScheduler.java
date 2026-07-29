package com.aiqaos.execution.scheduler;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.execution.engine.ExecutionConfiguration;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import com.aiqaos.execution.engine.ExecutionMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * WF-4: runs a script suite across the {@code browser × shard} matrix and aggregates one result.
 *
 * <p>A single-unit matrix (one browser, no shard — the default) runs the engine directly, so the
 * pre-WF-4 path is unchanged. A multi-unit matrix fans out: {@code PARALLEL} mode over a
 * virtual-thread executor (same technology as SCALE-1's queue), {@code SEQUENTIAL} inline. Cross-host
 * distribution is a later swap of this dispatch for SCALE-1's containerised worker tier — no change
 * to the planner/aggregator.
 */
@Component
public class ShardedExecutionScheduler {

    private static final Logger log = LoggerFactory.getLogger(ShardedExecutionScheduler.class);

    private final ExecutionEngineFactory engineFactory;
    private final ExecutionMatrixPlanner planner;
    private final ExecutionResultAggregator aggregator;

    public ShardedExecutionScheduler(ExecutionEngineFactory engineFactory,
                                     ExecutionMatrixPlanner planner,
                                     ExecutionResultAggregator aggregator) {
        this.engineFactory = engineFactory;
        this.planner = planner;
        this.aggregator = aggregator;
    }

    public ExecutionResult execute(GeneratedScriptSuite scriptSuite, ExecutionConfiguration config, String framework) {
        List<ShardPlan> plans = planner.plan(config);

        // Default / single-unit path: behave exactly as pre-WF-4 (no fan-out, no aggregation overhead).
        if (plans.size() <= 1) {
            ExecutionConfiguration unit = plans.isEmpty() ? config : planner.unitConfig(config, plans.get(0));
            return runUnit(scriptSuite, unit, framework);
        }

        boolean parallel = config.getExecutionMode() == ExecutionMode.PARALLEL;
        log.info("[WF-4] Fanning out {} unit(s) across the browser×shard matrix ({})",
                plans.size(), parallel ? "PARALLEL" : "SEQUENTIAL");
        List<ExecutionResult> results = parallel
                ? runParallel(scriptSuite, config, plans, framework)
                : runSequential(scriptSuite, config, plans, framework);
        return aggregator.merge(results);
    }

    private List<ExecutionResult> runSequential(GeneratedScriptSuite suite, ExecutionConfiguration config,
                                                List<ShardPlan> plans, String framework) {
        List<ExecutionResult> results = new ArrayList<>();
        for (ShardPlan plan : plans) {
            results.add(runUnit(suite, planner.unitConfig(config, plan), framework));
        }
        return results;
    }

    private List<ExecutionResult> runParallel(GeneratedScriptSuite suite, ExecutionConfiguration config,
                                              List<ShardPlan> plans, String framework) {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ExecutionResult>> futures = new ArrayList<>();
            for (ShardPlan plan : plans) {
                ExecutionConfiguration unit = planner.unitConfig(config, plan);
                futures.add(pool.submit(() -> runUnit(suite, unit, framework)));
            }
            List<ExecutionResult> results = new ArrayList<>();
            for (Future<ExecutionResult> future : futures) {
                results.add(future.get());
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Parallel matrix execution failed: " + e.getMessage(), e);
        }
    }

    private ExecutionResult runUnit(GeneratedScriptSuite suite, ExecutionConfiguration unit, String framework) {
        return engineFactory.getEngine(framework).execute(suite, unit);
    }
}
