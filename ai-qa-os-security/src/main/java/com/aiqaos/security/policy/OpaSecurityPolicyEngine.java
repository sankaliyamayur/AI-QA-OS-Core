package com.aiqaos.security.policy;

import com.aiqaos.security.context.SecurityContext;
import org.springframework.stereotype.Component;

/**
 * Future: delegates policy evaluation to Open Policy Agent (OPA) REST API.
 */
@Component
public class OpaSecurityPolicyEngine implements SecurityPolicyEngine {

    @Override
    public boolean evaluate(SecurityContext context, PolicyRequest request) {
        // TODO: POST to OPA /v1/data/{policy_path} endpoint
        return false;
    }
}