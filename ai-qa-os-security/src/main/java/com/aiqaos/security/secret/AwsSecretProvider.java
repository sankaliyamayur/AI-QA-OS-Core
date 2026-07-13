package com.aiqaos.security.secret;

import org.springframework.stereotype.Component;

@Component
public class AwsSecretProvider implements SecretManager {

    @Override
    public String getSecret(String key) {
        // TODO: Integrate AWS Secrets Manager SDK
        return null;
    }
}