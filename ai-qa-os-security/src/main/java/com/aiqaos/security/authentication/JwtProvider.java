package com.aiqaos.security.authentication;

import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    public boolean validate(String token) {
        return token != null && !token.isBlank();
    }

    public String extractUserId(String token) {
        return null;
    }

    public String generateToken(String userId) {
        return null;
    }
}