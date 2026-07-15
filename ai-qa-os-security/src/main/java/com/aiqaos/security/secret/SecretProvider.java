package com.aiqaos.security.secret;

public interface SecretProvider {
    String getSecret(String path);
}
