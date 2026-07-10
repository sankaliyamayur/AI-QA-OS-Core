package com.aiqaos.core.event;

public class SystemEvent extends BaseEvent {
    private String componentName;
    private String systemMessage;

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }
    public String getSystemMessage() { return systemMessage; }
    public void setSystemMessage(String systemMessage) { this.systemMessage = systemMessage; }
}