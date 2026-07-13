package com.aiqaos.security.authentication;

import org.springframework.stereotype.Component;

@Component
public class ServiceAccountProvider {

    public boolean validate(String serviceToken) {
        return false;
    }
}