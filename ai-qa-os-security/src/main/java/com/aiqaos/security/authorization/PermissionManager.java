package com.aiqaos.security.authorization;

import com.aiqaos.security.context.SecurityContext;
import org.springframework.stereotype.Component;

@Component
public class PermissionManager {

    public boolean hasPermission(SecurityContext context, String permission) {
        if (context == null || context.getPermissions() == null) return false;
        return context.getPermissions().contains(permission);
    }
}