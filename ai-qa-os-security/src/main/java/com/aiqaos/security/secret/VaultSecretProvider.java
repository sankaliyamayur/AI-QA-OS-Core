package com.aiqaos.security.secret;

import org.springframework.stereotype.Component;

@Component
public class VaultSecretProvider implements SecretManager {

    @Override
    public String getSecret(String key) {
        // TODO: Integrate Hashicorp Vault client
        return null;
    }
}