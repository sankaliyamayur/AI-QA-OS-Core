package com.aiqaos.dashboard.service;

import com.aiqaos.dashboard.dto.RbacAdminSummary;
import com.aiqaos.security.rbac.PermissionRepository;
import com.aiqaos.security.rbac.RoleRepository;
import com.aiqaos.security.rbac.UserRepository;
import org.springframework.stereotype.Service;

/**
 * ENT-4 (FI-ENT4-B): serves the secret-free RBAC admin read-model. Fetches the RBAC entities and
 * delegates to the pure {@link RbacAdminAssembler} (ADR-052). Read-only — mutating admin operations
 * (create/disable user, assign role) are deferred (FI-ENT4-A) to a UI with careful authz.
 */
@Service
public class RbacAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RbacAdminAssembler assembler;

    public RbacAdminService(UserRepository userRepository,
                            RoleRepository roleRepository,
                            PermissionRepository permissionRepository,
                            RbacAdminAssembler assembler) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.assembler = assembler;
    }

    public RbacAdminSummary getSummary() {
        return assembler.summarize(
                userRepository.findAll(),
                roleRepository.findAll(),
                permissionRepository.findAll());
    }
}
