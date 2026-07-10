package com.aiqaos.core.event;

public class ReportingEvent extends BaseEvent {
    private String reportPath;
    private boolean generatedSuccessfully;

    public String getReportPath() { return reportPath; }
    public void setReportPath(String reportPath) { this.reportPath = reportPath; }
    public boolean isGeneratedSuccessfully() { return generatedSuccessfully; }
    public void setGeneratedSuccessfully(boolean generatedSuccessfully) { this.generatedSuccessfully = generatedSuccessfully; }
}