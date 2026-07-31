package com.aiqaos.security.bootstrap;

import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.rbac.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * SEC-1 — creates a single bootstrap ADMIN user when enforcement is enabled and none exists yet.
 *
 * <p>No admin is seeded by any migration, so enabling authentication would otherwise lock everyone
 * out. The credential is read from configuration/environment and never committed (full secret
 * management is SEC-2). If no password is configured, or a user with the target username already
 * exists, this is a no-op. Any failure is logged and swallowed — it must never prevent the
 * application from starting (the security enforcement itself is the P0 deliverable).
 */
@Component
@ConditionalOnProperty(name = "aiqaos.security.enabled", havingValue = "true")
public class BootstrapAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminInitializer.class);

    private final ObjectProvider<UserRepository> userRepositoryProvider;
    private final String username;
    private final String email;
    private final String password;

    public BootstrapAdminInitializer(
            ObjectProvider<UserRepository> userRepositoryProvider,
            @Value("${aiqaos.security.bootstrap-admin.username:admin}") String username,
            @Value("${aiqaos.security.bootstrap-admin.email:admin@aiqaos.local}") String email,
            @Value("${aiqaos.security.bootstrap-admin.password:}") String password) {
        this.userRepositoryProvider = userRepositoryProvider;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (password == null || password.isBlank()) {
                log.warn("SEC-1: security enforcement is enabled but no bootstrap admin password is "
                        + "configured (aiqaos.security.bootstrap-admin.password). Provision an admin "
                        + "user manually, or set the property (via env/secret) to auto-create one.");
                return;
            }
            UserRepository userRepository = userRepositoryProvider.getIfAvailable();
            if (userRepository == null) {
                log.warn("SEC-1: UserRepository unavailable; skipping bootstrap admin creation.");
                return;
            }
            if (userRepository.findByUsername(username).isPresent()) {
                return; // already provisioned
            }
            UserEntity admin = new UserEntity();
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPasswordHash(new BCryptPasswordEncoder().encode(password));
            admin.setEnabled(true);
            admin.setRoles(java.util.List.of("ADMIN")); // FI-ENT4-C (ADR-066): a real ADMIN principal
            userRepository.save(admin);
            log.info("SEC-1: created bootstrap admin user '{}'.", username);
        } catch (Exception ex) {
            log.error("SEC-1: bootstrap admin creation failed (application startup continues). "
                    + "Provision an admin user manually. Cause: {}", ex.getMessage());
        }
    }
}
