package com.aiqaos.healing.engine;

import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.LearningResult;
import com.aiqaos.core.model.SelfHealingResult;

public interface SelfHealingEngine {
    SelfHealingResult heal(ExecutionResult executionResult, LearningResult learningResult, BugAnalysisReport bugReport);
}
