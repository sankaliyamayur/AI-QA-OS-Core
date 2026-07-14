package com.aiqaos.core.context;

import com.aiqaos.core.requirement.RequirementContext;
import com.aiqaos.core.model.QAAnalysisResult;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.LearningFeedback;
import com.aiqaos.core.model.QAExecutionReport;
import com.aiqaos.core.model.LearningResult;
import java.io.Serializable;

public class AutonomousQAWorkflowState implements Serializable {
    private static final long serialVersionUID = 1L;

    private RequirementContext requirementContext;
    private QAAnalysisResult qaAnalysisResult;
    private GeneratedTestCaseSuite generatedTestCaseSuite;
    private GeneratedScriptSuite generatedScriptSuite;
    private ExecutionResult executionResult;
    private BugAnalysisReport bugAnalysisReport;
    private LearningFeedback learningFeedback;
    private QAExecutionReport qaExecutionReport;
    private LearningResult learningResult;

    public AutonomousQAWorkflowState() {}

    public RequirementContext getRequirementContext() { return requirementContext; }
    public void setRequirementContext(RequirementContext requirementContext) { this.requirementContext = requirementContext; }

    public QAAnalysisResult getQaAnalysisResult() { return qaAnalysisResult; }
    public void setQaAnalysisResult(QAAnalysisResult qaAnalysisResult) { this.qaAnalysisResult = qaAnalysisResult; }

    public GeneratedTestCaseSuite getGeneratedTestCaseSuite() { return generatedTestCaseSuite; }
    public void setGeneratedTestCaseSuite(GeneratedTestCaseSuite generatedTestCaseSuite) { this.generatedTestCaseSuite = generatedTestCaseSuite; }

    public GeneratedScriptSuite getGeneratedScriptSuite() { return generatedScriptSuite; }
    public void setGeneratedScriptSuite(GeneratedScriptSuite generatedScriptSuite) { this.generatedScriptSuite = generatedScriptSuite; }

    public ExecutionResult getExecutionResult() { return executionResult; }
    public void setExecutionResult(ExecutionResult executionResult) { this.executionResult = executionResult; }

    public BugAnalysisReport getBugAnalysisReport() { return bugAnalysisReport; }
    public void setBugAnalysisReport(BugAnalysisReport bugAnalysisReport) { this.bugAnalysisReport = bugAnalysisReport; }

    public LearningFeedback getLearningFeedback() { return learningFeedback; }
    public void setLearningFeedback(LearningFeedback learningFeedback) { this.learningFeedback = learningFeedback; }

    public QAExecutionReport getQaExecutionReport() { return qaExecutionReport; }
    public void setQaExecutionReport(QAExecutionReport qaExecutionReport) { this.qaExecutionReport = qaExecutionReport; }

    public LearningResult getLearningResult() { return learningResult; }
    public void setLearningResult(LearningResult learningResult) { this.learningResult = learningResult; }
}
