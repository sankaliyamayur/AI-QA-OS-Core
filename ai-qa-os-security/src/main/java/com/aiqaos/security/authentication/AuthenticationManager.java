package com.aiqaos.security.authentication;

import com.aiqaos.security.context.SecurityContext;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationManager {

    public SecurityContext authenticate(String token) {
        // Delegates to JwtProvider / OAuth2Provider / ApiKeyProvider
        return new SecurityContext();
    }
}