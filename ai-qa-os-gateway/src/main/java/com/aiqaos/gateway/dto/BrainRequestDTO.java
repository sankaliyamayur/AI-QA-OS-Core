package com.aiqaos.gateway.dto;

public class BrainRequestDTO extends GatewayRequestDTO {
    private String requirement;
    private String requestType;

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
}