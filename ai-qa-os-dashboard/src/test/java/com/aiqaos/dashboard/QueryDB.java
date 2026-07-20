package com.aiqaos.dashboard;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class QueryDB {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/ai_qa_os_dashboard?preferQueryMode=simple", "postgres", "password");
            
            Statement stmt = conn.createStatement();
            
            System.out.println("--- LATEST WORKFLOW EXECUTIONS ---");
            ResultSet rs = stmt.executeQuery(
                "SELECT execution_id, status, current_step, result, created_at FROM workflow_executions ORDER BY created_at DESC LIMIT 3"
            );
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                System.out.println("Workflow Execution:");
                for (int i = 1; i <= cols; i++) {
                    System.out.println("  " + meta.getColumnName(i) + ": " + rs.getString(i));
                }
            }
            
            System.out.println("\n--- EXECUTIONS IN DB ---");
            rs = stmt.executeQuery("SELECT * FROM executions ORDER BY created_at DESC LIMIT 3");
            meta = rs.getMetaData();
            cols = meta.getColumnCount();
            while (rs.next()) {
                System.out.println("Execution:");
                for (int i = 1; i <= cols; i++) {
                    System.out.println("  " + meta.getColumnName(i) + ": " + rs.getString(i));
                }
            }
            
            System.out.println("\n--- ARTIFACTS IN DB ---");
            rs = stmt.executeQuery("SELECT * FROM execution_artifacts ORDER BY created_at DESC LIMIT 5");
            meta = rs.getMetaData();
            cols = meta.getColumnCount();
            while (rs.next()) {
                System.out.println("Artifact:");
                for (int i = 1; i <= cols; i++) {
                    System.out.println("  " + meta.getColumnName(i) + ": " + rs.getString(i));
                }
            }
            
            System.out.println("\n--- USERS IN DB ---");
            rs = stmt.executeQuery("SELECT id, username, email, enabled, account_locked, active, password_hash FROM security_users");
            meta = rs.getMetaData();
            cols = meta.getColumnCount();
            while (rs.next()) {
                System.out.println("User:");
                for (int i = 1; i <= cols; i++) {
                    System.out.println("  " + meta.getColumnName(i) + ": " + rs.getString(i));
                }
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
