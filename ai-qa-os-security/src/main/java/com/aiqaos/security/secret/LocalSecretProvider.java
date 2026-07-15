package com.aiqaos.security.secret;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class LocalSecretProvider implements SecretProvider, SecretManager {

    @Override
    public String getSecret(String path) {
        // Reads from encrypted local properties / env variables
        return System.getenv(path);
    }
}