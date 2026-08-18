package com.aiqaos.gateway.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GatewayDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GatewayDataInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public GatewayDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        seedUsers();
    }

    private void seedUsers() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM security_users", Integer.class);
            if (count == null || count == 0) {
                log.info("Seeding Gateway RBAC users (admin/admin)...");
                UUID adminId = UUID.randomUUID();
                UUID qaEngId = UUID.randomUUID();
                UUID qaMgrId = UUID.randomUUID();

                String validAdminHash = new BCryptPasswordEncoder().encode("admin");

                jdbcTemplate.update("""
                    INSERT INTO security_users (id, tenant_id, username, email, password_hash, enabled, account_locked, failed_login_attempts, mfa_enabled, created_at, active, deleted, version)
                    VALUES (?, '__system__', 'admin', 'admin@aiqaos.local', ?, true, false, 0, false, CURRENT_TIMESTAMP, true, false, 0)
                """, adminId, validAdminHash);

                jdbcTemplate.update("""
                    INSERT INTO security_users (id, tenant_id, username, email, password_hash, enabled, account_locked, failed_login_attempts, mfa_enabled, created_at, active, deleted, version)
                    VALUES (?, '__system__', 'qa_engineer', 'engineer@aiqaos.local', ?, true, false, 0, false, CURRENT_TIMESTAMP, true, false, 0)
                """, qaEngId, validAdminHash);

                jdbcTemplate.update("""
                    INSERT INTO security_users (id, tenant_id, username, email, password_hash, enabled, account_locked, failed_login_attempts, mfa_enabled, created_at, active, deleted, version)
                    VALUES (?, '__system__', 'qa_manager', 'manager@aiqaos.local', ?, true, false, 0, false, CURRENT_TIMESTAMP, true, false, 0)
                """, qaMgrId, validAdminHash);

                jdbcTemplate.update("INSERT INTO security_user_roles (user_id, role_name) VALUES (?, 'ADMIN')", adminId);
                jdbcTemplate.update("INSERT INTO security_user_roles (user_id, role_name) VALUES (?, 'QA_ENGINEER')", qaEngId);
                jdbcTemplate.update("INSERT INTO security_user_roles (user_id, role_name) VALUES (?, 'QA_MANAGER')", qaMgrId);
            }
        } catch (Exception e) {
            log.warn("GatewayDataInitializer could not seed security_users: {}", e.getMessage());
        }
    }
}
