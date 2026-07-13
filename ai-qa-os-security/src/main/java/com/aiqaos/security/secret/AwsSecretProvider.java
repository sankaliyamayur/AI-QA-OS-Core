package com.aiqaos.security.secret;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "aiqaos.security.secret.provider", havingValue = "aws")
public class AwsSecretProvider implements SecretManager {

    @Override
    public String getSecret(String key) {
        // TODO: Integrate AWS Secrets Manager SDK
        return null;
    }
}