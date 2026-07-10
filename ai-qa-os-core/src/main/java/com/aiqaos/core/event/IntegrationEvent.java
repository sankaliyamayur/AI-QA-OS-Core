package com.aiqaos.core.event;

public class IntegrationEvent extends BaseEvent {
    private String externalTarget;
    private String transmissionType;

    public String getExternalTarget() { return externalTarget; }
    public void setExternalTarget(String externalTarget) { this.externalTarget = externalTarget; }
    public String getTransmissionType() { return transmissionType; }
    public void setTransmissionType(String transmissionType) { this.transmissionType = transmissionType; }
}