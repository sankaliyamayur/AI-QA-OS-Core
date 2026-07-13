package com.aiqaos.security.policy;

import com.aiqaos.security.context.SecurityContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultPolicyEngine implements SecurityPolicyEngine {

    @Override
    public boolean evaluate(SecurityContext context, PolicyRequest request) {
        if (context == null || context.getPermissions() == null) {
            return false;
        }
        return context.getPermissions().contains(request.getAction());
    }
}