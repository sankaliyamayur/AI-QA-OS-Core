package com.aiqaos.execution.manager;

import com.aiqaos.core.engine.ExecutionEngine;
import com.aiqaos.core.contract.ExecutionRequest;
import com.aiqaos.core.contract.ExecutionResponse;
import com.aiqaos.execution.dispatcher.ExecutionDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionManagerImpl implements ExecutionEngine<ExecutionRequest, ExecutionResponse> {

    @Autowired
    private ExecutionDispatcher dispatcher;

    @Override
    public ExecutionResponse execute(ExecutionRequest request) {
        return dispatcher.dispatch(request);
    }
}