package com.aiqaos.security.admin;

import com.aiqaos.security.admin.AdminUserRequests.CreateUserRequest;
import com.aiqaos.security.admin.AdminUserRequests.EnabledRequest;
import com.aiqaos.security.admin.AdminUserRequests.RolesRequest;
import com.aiqaos.security.rbac.UserEntity;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FI-ENT4-A (ADR-067): the admin write API — create / enable-disable / assign-roles for users.
 *
 * <p>Hosted at {@code /api/admin/**}, deliberately <b>outside</b> {@code /api/dashboard/**}: that
 * prefix is not in {@code DashboardSecurityConfig}'s permissive matcher, so requests fall onto
 * {@code SecurityConfig.enforcedFilterChain} where the JWT filter runs. Combined with the class-level
 * {@code @PreAuthorize("hasRole('ADMIN')")} (method security is global) this fails closed: an
 * unauthenticated or non-admin caller is rejected regardless of which filter chain matched.
 *
 * <p>Read access stays on the open, secret-free {@code /api/dashboard/admin/rbac} (FI-ENT4-B); only
 * mutations require ADMIN.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(name = "aiqaos.security.database-enabled", havingValue = "true", matchIfMissing = true)
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public ResponseEntity<AdminUserView> create(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.create(request));
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<AdminUserView> setEnabled(@PathVariable("id") UUID id,
                                                    @RequestBody EnabledRequest request,
                                                    @AuthenticationPrincipal UserEntity caller) {
        return ResponseEntity.ok(adminUserService.setEnabled(id, request.enabled(), callerId(caller)));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<AdminUserView> setRoles(@PathVariable("id") UUID id,
                                                  @RequestBody RolesRequest request,
                                                  @AuthenticationPrincipal UserEntity caller) {
        return ResponseEntity.ok(adminUserService.setRoles(id, request.roles(), callerId(caller)));
    }

    /** The authenticated admin's own id, used for the self-lockout / self-demotion guards (null-safe). */
    private static UUID callerId(UserEntity caller) {
        return caller != null ? caller.getId() : null;
    }
}
