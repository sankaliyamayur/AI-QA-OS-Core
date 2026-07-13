package com.aiqaos.execution.provider;

import com.aiqaos.core.contract.ExecutionRequest;
import com.aiqaos.core.contract.ExecutionResponse;
import org.springframework.stereotype.Component;

@Component
public class SeleniumExecutionProvider implements ExecutionProvider {
    @Override
    public String getProviderType() { return "Selenium"; }
    @Override
    public ExecutionResponse execute(ExecutionRequest request) {
        ExecutionResponse res = new ExecutionResponse();
        res.getMetadata().setWorkflowId(request.getMetadata().getWorkflowId());
        res.setStatus("SUCCESS");
        res.setMessage("Selenium run completed for: " + request.getScriptContent());
        res.setExitCode(0);
        res.setStdout("Mock Selenium test run output.");
        return res;
    }
}