package com.aiqaos.security.rbac;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "security_api_keys")
public class ApiKeyEntity extends BaseEntity {

    @Column(name = "key_hash", nullable = false, unique = true)
    private String keyHash;

    @Column(name = "secret_hash", nullable = false)
    private String secretHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "permission")
    private List<String> permissions = new ArrayList<>();

    @Column(name = "expiry")
    private Instant expiry;

    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public String getSecretHash() { return secretHash; }
    public void setSecretHash(String secretHash) { this.secretHash = secretHash; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public Instant getExpiry() { return expiry; }
    public void setExpiry(Instant expiry) { this.expiry = expiry; }
}
