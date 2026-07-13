package com.aiqaos.security.secret;

import org.springframework.stereotype.Component;

@Component
public class LocalSecretProvider implements SecretManager {

    @Override
    public String getSecret(String key) {
        // Reads from encrypted local properties â€” for development only
        return System.getenv(key);
    }
}