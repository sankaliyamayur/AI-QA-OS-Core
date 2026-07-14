package com.aiqaos.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QAExecutionReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reportId;
    private String reportVersion;
    private String executionId;
    private String status;          // COMPLETED / PARTIAL / FAILED
    private String summary;
    private int totalTestCases;
    private int passedTests;
    private int failedTests;
    private double passPercentage;
    private String overallResult;   // PASS / FAIL / PARTIAL
    private List<String> recommendations = new ArrayList<>();
    private String generatedBy;
    private QAAnalysisResult qaAnalysisResult;
    private GeneratedTestCaseSuite testSuite;
    private GeneratedScriptSuite scriptSuite;
    private ExecutionResult executionResult;
    private BugAnalysisReport bugAnalysisReport;
    private LocalDateTime createdTime;

    public QAExecutionReport() {}

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReportVersion() { return reportVersion; }
    public void setReportVersion(String reportVersion) { this.reportVersion = reportVersion; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getTotalTestCases() { return totalTestCases; }
    public void setTotalTestCases(int totalTestCases) { this.totalTestCases = totalTestCases; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }

    public double getPassPercentage() { return passPercentage; }
    public void setPassPercentage(double passPercentage) { this.passPercentage = passPercentage; }

    public String getOverallResult() { return overallResult; }
    public void setOverallResult(String overallResult) { this.overallResult = overallResult; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public QAAnalysisResult getQaAnalysisResult() { return qaAnalysisResult; }
    public void setQaAnalysisResult(QAAnalysisResult qaAnalysisResult) { this.qaAnalysisResult = qaAnalysisResult; }

    public GeneratedTestCaseSuite getTestSuite() { return testSuite; }
    public void setTestSuite(GeneratedTestCaseSuite testSuite) { this.testSuite = testSuite; }

    public GeneratedScriptSuite getScriptSuite() { return scriptSuite; }
    public void setScriptSuite(GeneratedScriptSuite scriptSuite) { this.scriptSuite = scriptSuite; }

    public ExecutionResult getExecutionResult() { return executionResult; }
    public void setExecutionResult(ExecutionResult executionResult) { this.executionResult = executionResult; }

    public BugAnalysisReport getBugAnalysisReport() { return bugAnalysisReport; }
    public void setBugAnalysisReport(BugAnalysisReport bugAnalysisReport) { this.bugAnalysisReport = bugAnalysisReport; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
