package com.aiqaos.security.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.security.rbac.UserEntity;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

/**
 * FI-ENT4-C (ADR-066): authorities are derived from the user's persisted roles — baseline ROLE_USER
 * plus ROLE_&lt;name&gt; per role — so hasRole('ADMIN') works for an ADMIN user.
 */
class AuthorityMapperTest {

    private Set<String> names(UserEntity user) {
        return AuthorityMapper.authorities(user).stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    private UserEntity userWithRoles(List<String> roles) {
        UserEntity user = new UserEntity();
        user.setRoles(roles);
        return user;
    }

    @Test
    void adminRole_yieldsRoleAdmin() {
        Set<String> auths = names(userWithRoles(List.of("ADMIN")));
        assertTrue(auths.contains("ROLE_USER"), "baseline kept");
        assertTrue(auths.contains("ROLE_ADMIN"), "ADMIN role -> ROLE_ADMIN");
        assertEquals(2, auths.size());
    }

    @Test
    void noRoles_yieldsBaselineOnly() {
        assertEquals(Set.of("ROLE_USER"), names(userWithRoles(List.of())));
    }

    @Test
    void nullUser_yieldsBaselineOnly() {
        assertEquals(Set.of("ROLE_USER"), names(null));
    }

    @Test
    void normalizesCaseAndPrefix_ignoresBlanksAndDuplicates() {
        Set<String> auths = names(userWithRoles(List.of("admin", "ROLE_ADMIN", "  ", "QA_Manager")));
        assertTrue(auths.contains("ROLE_ADMIN"));
        assertTrue(auths.contains("ROLE_QA_MANAGER"));
        assertEquals(Set.of("ROLE_USER", "ROLE_ADMIN", "ROLE_QA_MANAGER"), auths, "deduped + blanks skipped");
    }
}
