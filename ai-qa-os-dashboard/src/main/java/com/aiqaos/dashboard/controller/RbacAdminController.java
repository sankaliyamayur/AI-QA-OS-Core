package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.dto.RbacAdminSummary;
import com.aiqaos.dashboard.service.RbacAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ENT-4 (FI-ENT4-B): read-only RBAC admin surface. Exposes the secret-free {@link RbacAdminSummary}
 * (user/role/permission counts, security posture, per-user views — never password hashes or MFA
 * secrets). Mutating admin operations are deferred (FI-ENT4-A) to a write API with careful authz.
 */
@RestController
@RequestMapping("/api/dashboard/admin/rbac")
public class RbacAdminController {

    private final RbacAdminService rbacAdminService;

    public RbacAdminController(RbacAdminService rbacAdminService) {
        this.rbacAdminService = rbacAdminService;
    }

    @GetMapping
    public RbacAdminSummary getSummary() {
        return rbacAdminService.getSummary();
    }
}
