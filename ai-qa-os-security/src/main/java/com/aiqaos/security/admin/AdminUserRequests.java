package com.aiqaos.security.admin;

import java.util.List;

/**
 * FI-ENT4-A (ADR-067): request bodies for the admin write API. Grouped as one file — small, cohesive,
 * write-only records. Tenant is never a field: it is bound authoritatively from the caller's JWT
 * ({@code @TenantId}, ENT-1/ADR-055), never accepted from the client.
 */
public final class AdminUserRequests {

    private AdminUserRequests() {}

    /** Create a user. Password is plaintext on the wire (TLS) and BCrypt-hashed before persist. */
    public record CreateUserRequest(String username, String email, String password, List<String> roles) {}

    /** Enable or disable (soft) a user. */
    public record EnabledRequest(boolean enabled) {}

    /** Replace a user's assigned role names (validated against the global catalog). */
    public record RolesRequest(List<String> roles) {}
}
