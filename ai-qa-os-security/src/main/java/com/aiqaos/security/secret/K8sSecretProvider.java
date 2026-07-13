package com.aiqaos.security.secret;

import org.springframework.stereotype.Component;

@Component
public class K8sSecretProvider implements SecretManager {

    @Override
    public String getSecret(String key) {
        // TODO: Read from Kubernetes mounted secret volume
        return null;
    }
}