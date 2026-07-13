package com.aiqaos.gateway.exception;

public class ResourceNotFoundException extends GatewayException {
    public ResourceNotFoundException(String resource, String id) {
        super(resource + " not found with id: " + id, 404, "RESOURCE_NOT_FOUND");
    }
}