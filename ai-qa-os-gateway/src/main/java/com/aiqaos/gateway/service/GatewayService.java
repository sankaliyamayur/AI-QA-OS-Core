package com.aiqaos.gateway.service;

import com.aiqaos.gateway.event.GatewayEventPublisher;
import com.aiqaos.security.ratelimit.RateLimiter;
import com.aiqaos.security.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GatewayService {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final GatewayEventPublisher eventPublisher;
    protected final RateLimiter rateLimiter;

    protected GatewayService(GatewayEventPublisher eventPublisher, RateLimiter rateLimiter) {
        this.eventPublisher = eventPublisher;
        this.rateLimiter = rateLimiter;
    }

    protected String currentUserId() {
        var ctx = SecurityContextHolder.getContext();
        return ctx != null ? ctx.getUserId() : "anonymous";
    }

    protected boolean checkRateLimit(String userId) {
        return rateLimiter.isAllowed(userId);
    }
}