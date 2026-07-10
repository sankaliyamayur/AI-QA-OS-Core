package com.aiqaos.core.contract;

public class AgentResponse extends BaseResponse {
    private String content;
    private double confidenceScore;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
}