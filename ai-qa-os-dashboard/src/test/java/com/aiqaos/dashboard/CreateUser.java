package com.aiqaos.dashboard;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CreateUser {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/ai_qa_os_dashboard?preferQueryMode=simple", "postgres", "password");
            
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String hash = encoder.encode("admin");
            
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO security_users (id, username, email, password_hash, enabled, created_at, active, deleted, failed_login_attempts, account_locked, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            
            ps.setObject(1, UUID.randomUUID());
            ps.setString(2, "admin");
            ps.setString(3, "admin@aiqaos.com");
            ps.setString(4, hash);
            ps.setBoolean(5, true);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.setBoolean(7, true);
            ps.setBoolean(8, false);
            ps.setInt(9, 0);
            ps.setBoolean(10, false);
            ps.setLong(11, 0L);
            
            int rows = ps.executeUpdate();
            System.out.println("User 'admin' created: " + rows + " row(s) inserted.");
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
