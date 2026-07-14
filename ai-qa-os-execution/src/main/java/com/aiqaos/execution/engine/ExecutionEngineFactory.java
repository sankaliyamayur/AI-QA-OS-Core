package com.aiqaos.execution.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExecutionEngineFactory {
    private final Map<String, ExecutionEngine> engines = new HashMap<>();

    @Autowired
    public ExecutionEngineFactory(List<ExecutionEngine> engineList) {
        for (ExecutionEngine engine : engineList) {
            engines.put(engine.getSupportedFramework().toUpperCase(), engine);
        }
    }

    public ExecutionEngine getEngine(String framework) {
        if (framework == null) {
            throw new IllegalArgumentException("Framework cannot be null");
        }
        ExecutionEngine engine = engines.get(framework.toUpperCase());
        if (engine == null) {
            throw new IllegalArgumentException("Unsupported framework: " + framework);
        }
        return engine;
    }
}
