package com.aiqaos.eval.dataset;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.GoldenDatasetProvider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Reference {@link GoldenDatasetProvider}: suites registered in memory. Enough to run
 * evaluations today; <b>PE-1</b> replaces this with durable, versioned datasets.
 */
@Component
public class InMemoryGoldenDatasetProvider implements GoldenDatasetProvider {

    private final Map<String, List<EvaluationCase>> suites = new ConcurrentHashMap<>();

    /** Register (or replace) the cases for a suite. */
    public void put(String suite, List<EvaluationCase> cases) {
        suites.put(suite, List.copyOf(cases));
    }

    @Override
    public List<EvaluationCase> load(String suite) {
        return suites.getOrDefault(suite, List.of());
    }
}
