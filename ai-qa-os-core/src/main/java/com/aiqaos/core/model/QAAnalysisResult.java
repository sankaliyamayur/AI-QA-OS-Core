package com.aiqaos.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QAAnalysisResult {
    private String workflowId;
    private String analysisSummary;
    private List<String> identifiedScenarios = new ArrayList<>();
    private Map<String, String> riskMatrix = new HashMap<>();
    private boolean readyForTestDesign;

    public QAAnalysisResult() {}

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getAnalysisSummary() { return analysisSummary; }
    public void setAnalysisSummary(String analysisSummary) { this.analysisSummary = analysisSummary; }

    public List<String> getIdentifiedScenarios() { return identifiedScenarios; }
    public void setIdentifiedScenarios(List<String> identifiedScenarios) { this.identifiedScenarios = identifiedScenarios; }

    public Map<String, String> getRiskMatrix() { return riskMatrix; }
    public void setRiskMatrix(Map<String, String> riskMatrix) { this.riskMatrix = riskMatrix; }

    public boolean isReadyForTestDesign() { return readyForTestDesign; }
    public void setReadyForTestDesign(boolean readyForTestDesign) { this.readyForTestDesign = readyForTestDesign; }
}
