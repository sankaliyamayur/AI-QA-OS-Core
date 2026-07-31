package com.aiqaos.security.authorization;

import com.aiqaos.security.rbac.UserEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * FI-ENT4-C (ADR-066): derives Spring authorities from a user's persisted roles. Every authenticated
 * principal keeps the baseline {@code ROLE_USER}; each assigned role adds {@code ROLE_<NAME>} (so an
 * ADMIN-role user carries {@code ROLE_ADMIN} and {@code hasRole('ADMIN')} works). Blanks and duplicates
 * are ignored; a role already prefixed with {@code ROLE_} is used as-is.
 */
public final class AuthorityMapper {

    private AuthorityMapper() {
    }

    public static List<GrantedAuthority> authorities(UserEntity user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (user == null || user.getRoles() == null) {
            return authorities;
        }
        for (String role : user.getRoles()) {
            if (role == null || role.isBlank()) {
                continue;
            }
            String normalized = role.trim().toUpperCase();
            String authority = normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
            boolean present = authorities.stream().anyMatch(a -> a.getAuthority().equals(authority));
            if (!present) {
                authorities.add(new SimpleGrantedAuthority(authority));
            }
        }
        return authorities;
    }
}
