package com.aiqaos.security.secret;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "aiqaos.security.secret.provider", havingValue = "k8s")
public class K8sSecretProvider implements SecretManager {

    @Override
    public String getSecret(String key) {
        // TODO: Read from Kubernetes mounted secret volume
        return null;
    }
}