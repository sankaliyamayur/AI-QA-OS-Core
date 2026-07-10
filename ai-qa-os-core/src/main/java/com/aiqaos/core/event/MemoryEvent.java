package com.aiqaos.core.event;

public class MemoryEvent extends BaseEvent {
    private String interactionContent;
    private boolean isVectorStored;

    public String getInteractionContent() { return interactionContent; }
    public void setInteractionContent(String interactionContent) { this.interactionContent = interactionContent; }
    public boolean isVectorStored() { return isVectorStored; }
    public void setVectorStored(boolean isVectorStored) { this.isVectorStored = isVectorStored; }
}