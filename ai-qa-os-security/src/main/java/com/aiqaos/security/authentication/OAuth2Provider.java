package com.aiqaos.security.authentication;

import org.springframework.stereotype.Component;

@Component
public class OAuth2Provider {

    public boolean validate(String accessToken) {
        return false;
    }
}