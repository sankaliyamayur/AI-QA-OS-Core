package com.aiqaos.learning.engine;

import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.model.QAExecutionReport;
import com.aiqaos.core.contract.AgentResponse;

public interface LearningEngine {
    AgentResponse analyze(QAExecutionReport report, WorkflowContext context);
}
