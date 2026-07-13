package com.aiqaos.security.authentication;

import org.springframework.stereotype.Component;

@Component
public class ApiKeyProvider {

    public boolean validate(String apiKey) {
        return apiKey != null && !apiKey.isBlank();
    }
}