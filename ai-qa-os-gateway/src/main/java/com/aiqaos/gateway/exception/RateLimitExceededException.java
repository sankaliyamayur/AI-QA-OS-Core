package com.aiqaos.gateway.exception;

public class RateLimitExceededException extends GatewayException {
    public RateLimitExceededException(String userId) {
        super("Rate limit exceeded for user: " + userId, 429, "RATE_LIMIT_EXCEEDED");
    }
}