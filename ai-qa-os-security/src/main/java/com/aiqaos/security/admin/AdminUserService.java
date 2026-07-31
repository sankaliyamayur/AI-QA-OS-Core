package com.aiqaos.security.admin;

import com.aiqaos.security.admin.AdminUserRequests.CreateUserRequest;
import com.aiqaos.security.rbac.RoleEntity;
import com.aiqaos.security.rbac.RoleRepository;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.rbac.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * FI-ENT4-A (ADR-067): the mutating half of the admin surface — create, enable/disable, and role
 * assignment for users. Every operation is tenant-scoped by construction: {@code @TenantId} stamps
 * new users with the caller's JWT-bound tenant and filters reads, so an admin only ever sees and
 * mutates users in their own tenant (ENT-1 invariant, ADR-055/058).
 *
 * <p>Authorization ({@code hasRole('ADMIN')}) is enforced at the controller. This service adds the
 * business guards that authz alone cannot: no self-lockout (disabling your own account), no
 * self-demotion (removing your own ADMIN role) — either would let an admin lock the tenant out via
 * the UI. Role names are validated against the global catalog so admins cannot invent authorities.
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /** Creates a tenant-scoped user with a hashed password and validated roles. */
    public AdminUserView create(CreateUserRequest req) {
        String username = require(req.username(), "username");
        String email = require(req.email(), "email");
        String password = require(req.password(), "password");

        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRoles(validateRoles(req.roles())); // tenant is stamped by @TenantId on insert
        return AdminUserView.from(userRepository.save(user));
    }

    /** Enables or disables a user. An admin may not disable their own account. */
    public AdminUserView setEnabled(UUID userId, boolean enabled, UUID callerId) {
        if (!enabled && userId.equals(callerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "an admin cannot disable their own account");
        }
        UserEntity user = load(userId);
        user.setEnabled(enabled);
        return AdminUserView.from(userRepository.save(user));
    }

    /** Replaces a user's roles (validated). An admin may not remove ADMIN from their own account. */
    public AdminUserView setRoles(UUID userId, List<String> roles, UUID callerId) {
        List<String> validated = validateRoles(roles);
        if (userId.equals(callerId) && !containsIgnoreCase(validated, "ADMIN")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "an admin cannot remove their own ADMIN role");
        }
        UserEntity user = load(userId);
        user.setRoles(validated);
        return AdminUserView.from(userRepository.save(user));
    }

    private UserEntity load(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    /**
     * Validates each requested role name against the global catalog (case-insensitively) and returns
     * the catalog's canonical names, de-duplicated. An unknown role is a 400 — an admin cannot mint a
     * role that no {@code RoleEntity} backs (ADR-066 left the names un-FK'd; this is the write guard).
     */
    private List<String> validateRoles(List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, String> catalog = roleRepository.findAll().stream()
                .map(RoleEntity::getRoleName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toMap(name -> name.trim().toUpperCase(), Function.identity(), (a, b) -> a));

        List<String> canonical = new ArrayList<>();
        for (String raw : requested) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String match = catalog.get(raw.trim().toUpperCase());
            if (match == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown role: " + raw);
            }
            if (!canonical.contains(match)) {
                canonical.add(match);
            }
        }
        return canonical;
    }

    private static boolean containsIgnoreCase(List<String> roles, String target) {
        return roles.stream().anyMatch(r -> r != null && r.trim().equalsIgnoreCase(target));
    }
}
