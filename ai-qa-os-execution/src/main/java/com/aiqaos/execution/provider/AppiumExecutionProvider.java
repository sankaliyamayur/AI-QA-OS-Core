package com.aiqaos.execution.provider;

import com.aiqaos.core.contract.ExecutionRequest;
import com.aiqaos.core.contract.ExecutionResponse;
import org.springframework.stereotype.Component;

@Component
public class AppiumExecutionProvider implements ExecutionProvider {
    @Override
    public String getProviderType() { return "Appium"; }
    @Override
    public ExecutionResponse execute(ExecutionRequest request) {
        ExecutionResponse res = new ExecutionResponse();
        res.getMetadata().setWorkflowId(request.getMetadata().getWorkflowId());
        res.setStatus("SUCCESS");
        res.setMessage("Appium mobile run completed for: " + request.getScriptContent());
        res.setExitCode(0);
        res.setStdout("Mock Appium test run output.");
        return res;
    }
}