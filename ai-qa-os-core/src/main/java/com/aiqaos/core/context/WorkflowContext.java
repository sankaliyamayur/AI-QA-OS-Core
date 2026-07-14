package com.aiqaos.core.context;

import com.aiqaos.core.contract.BaseContext;
import com.aiqaos.core.enums.WorkflowStatus;
import java.util.HashMap;
import java.util.Map;

public class WorkflowContext extends BaseContext {
    private String currentStep;
    private String previousStep;
    private String nextStep;
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, Object> sharedMemory = new HashMap<>();
    private int retryCount;
    private String state;
    private WorkflowStatus status;
    private AutonomousQAWorkflowState qaWorkflowState;

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
    
    public String getPreviousStep() { return previousStep; }
    public void setPreviousStep(String previousStep) { this.previousStep = previousStep; }
    
    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }
    
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    
    public Map<String, Object> getSharedMemory() { return sharedMemory; }
    public void setSharedMemory(Map<String, Object> sharedMemory) { this.sharedMemory = sharedMemory; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public WorkflowStatus getStatus() { return status; }
    public void setStatus(WorkflowStatus status) { this.status = status; }

    public AutonomousQAWorkflowState getQaWorkflowState() { return qaWorkflowState; }
    public void setQaWorkflowState(AutonomousQAWorkflowState qaWorkflowState) { this.qaWorkflowState = qaWorkflowState; }
}