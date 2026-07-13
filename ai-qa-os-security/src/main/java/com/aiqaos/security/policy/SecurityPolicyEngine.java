package com.aiqaos.security.policy;

import com.aiqaos.security.context.SecurityContext;

public interface SecurityPolicyEngine {
    boolean evaluate(SecurityContext context, PolicyRequest request);
}