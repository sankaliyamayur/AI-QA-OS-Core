package com.aiqaos.healing.engine;

import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.LearningResult;
import com.aiqaos.core.model.RecoveryAttempt;
import com.aiqaos.core.model.SelfHealingResult;
import com.aiqaos.execution.engine.ExecutionConfiguration;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import com.aiqaos.healing.memory.RecoveryHistoryStore;
import com.aiqaos.healing.service.ScriptGenerationService;
import com.aiqaos.healing.strategy.RecoveryStrategyResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SelfHealingEngineImpl implements SelfHealingEngine {

    @Autowired
    private RecoveryStrategyResolver strategyResolver;

    @Autowired
    private RecoveryHistoryStore historyStore;

    @Autowired
    private ExecutionEngineFactory executionEngineFactory;

    @Autowired
    private ScriptGenerationService scriptGenerationService;

    private static final int MAX_HEALING_ATTEMPTS = 3;

    @Override
    public SelfHealingResult heal(ExecutionResult executionResult, LearningResult learningResult, BugAnalysisReport bugReport) {
        SelfHealingResult result = new SelfHealingResult();
        result.setHealingId("HL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        result.setOriginalExecution(executionResult);
        result.setOriginalFailure(executionResult.getErrorMessage());
        result.setFailureCategory(bugReport != null ? bugReport.getFailureCategory() : "GENERIC_ERROR");

        String executionId = executionResult.getExecutionId() != null 
                ? executionResult.getExecutionId() 
                : UUID.randomUUID().toString();

        // 1. Get attempts history for this execution to check loop protection
        List<RecoveryAttempt> history = historyStore.getAttempts(executionId);
        int currentAttemptNumber = history.size() + 1;
        result.setRetryCount(currentAttemptNumber);

        if (currentAttemptNumber > MAX_HEALING_ATTEMPTS) {
            result.setHealingApplied(false);
            result.setRetrySuccessful(false);
            result.setRecoveryStatus("MAX_ATTEMPTS_EXCEEDED");
            result.setAppliedFix("Terminated self-healing execution loop due to MAX_HEALING_ATTEMPTS limit (3)");
            return result;
        }

        // 2. Resolve healing strategy
        String healingAction = "RETRY_ONLY";
        String appliedFix = "Simple Retry";
        if (learningResult != null && learningResult.getRecommendations() != null && !learningResult.getRecommendations().isEmpty()) {
            com.aiqaos.core.model.SelfHealingRecommendation recommendation = learningResult.getRecommendations().get(0);
            if (recommendation.getActionType() != null) {
                healingAction = recommendation.getActionType().name();
            } else if (recommendation.getSuggestedAction() != null) {
                healingAction = recommendation.getSuggestedAction();
            }
            appliedFix = recommendation.getSuggestedAction();
        }

        String strategy = strategyResolver.resolveStrategy(healingAction);
        result.setHealingStrategy(strategy);
        result.setActionType(healingAction);
        result.setAppliedFix(appliedFix);

        // 3. Log this attempt
        RecoveryAttempt attempt = new RecoveryAttempt();
        attempt.setAttemptId("ATT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        attempt.setExecutionId(executionId);
        attempt.setStrategy(strategy);
        attempt.setAttemptNumber(currentAttemptNumber);
        attempt.setErrorBefore(executionResult.getErrorMessage());
        attempt.setStatus("IN_PROGRESS");

        // 4. Implement Direct Retry through ExecutionEngine
        try {
            // Resolve framework execution engine
            String framework = "Playwright";
            if (bugReport != null && bugReport.getAffectedScripts() != null && !bugReport.getAffectedScripts().isEmpty()) {
                // Heuristic framework lookup or defaults to Playwright
                framework = "Playwright";
            }
            ExecutionEngine engine = executionEngineFactory.getEngine(framework);

            // Execute retry directly through resolved engine using empty/default execution config
            com.aiqaos.execution.engine.ExecutionConfiguration config = new com.aiqaos.execution.engine.ExecutionConfiguration();
            config.setExecutionMode(com.aiqaos.execution.engine.ExecutionMode.SEQUENTIAL);
            config.setEnvironment(com.aiqaos.execution.engine.EnvironmentType.DEV);
            config.setTimeout(30000);

            // Retrieve scripts. If regeneration requested, execute via ScriptGenerationService
            GeneratedScriptSuite scripts = new GeneratedScriptSuite();
            if ("ScriptRegenerationStrategy".equals(strategy)) {
                com.aiqaos.core.model.QAExecutionReport tempReport = new com.aiqaos.core.model.QAExecutionReport();
                tempReport.setExecutionResult(executionResult);
                tempReport.setBugAnalysisReport(bugReport);
                // Packaging placeholders
                scripts = scriptGenerationService.regenerate(tempReport, appliedFix);
            }

            ExecutionResult retryResult = engine.execute(scripts, config);

            // Populate healed results
            result.setHealedExecution(retryResult);
            result.setHealingApplied(true);
            
            if (retryResult.isSuccess()) {
                result.setRetrySuccessful(true);
                result.setRecoveryStatus("SUCCESS");
                result.setImprovementScore(1.0);
                attempt.setStatus("SUCCESS");
                attempt.setResultAfter("PASS");
            } else {
                result.setRetrySuccessful(false);
                result.setRecoveryStatus("FAILED");
                result.setImprovementScore(0.0);
                attempt.setStatus("FAILED");
                attempt.setResultAfter("FAIL: " + retryResult.getErrorMessage());
            }

        } catch (Exception ex) {
            result.setHealingApplied(true);
            result.setRetrySuccessful(false);
            result.setRecoveryStatus("ERROR");
            result.setAppliedFix(appliedFix + " (Error: " + ex.getMessage() + ")");
            attempt.setStatus("ERROR");
            attempt.setResultAfter("EXCEPTION: " + ex.getMessage());
        }

        // Store attempt record in MemoryStore
        historyStore.storeAttempt(executionId, attempt);

        return result;
    }
}
