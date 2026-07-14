package com.aiqaos.execution.engine;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;

public interface ExecutionEngine {
    String getSupportedFramework();
    ExecutionResult execute(GeneratedScriptSuite scriptSuite, ExecutionConfiguration configuration);
    void cancel();
    boolean isRunning();
}
