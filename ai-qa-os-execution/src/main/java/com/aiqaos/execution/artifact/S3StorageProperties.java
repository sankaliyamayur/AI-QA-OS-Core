package com.aiqaos.execution.artifact;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ENT-5 (ADR-068): configuration for the S3/MinIO object-storage binding.
 *
 * <p>SEC-2: {@code accessKey}/{@code secretKey} are injected from env/secret store, never committed
 * (the compose stack's {@code .env} already carries {@code MINIO_ROOT_USER}/{@code MINIO_ROOT_PASSWORD}).
 * {@code endpoint} is set for MinIO or any S3-compatible store and left blank for real AWS S3;
 * {@code pathStyleAccess} defaults to {@code true} because MinIO requires path-style addressing.
 */
@ConfigurationProperties(prefix = "aiqaos.artifacts.object.s3")
public class S3StorageProperties {

    private String bucket = "aiqaos-artifacts";
    private String endpoint = "";
    private String region = "us-east-1";
    private String accessKey = "";
    private String secretKey = "";
    private boolean pathStyleAccess = true;

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public boolean isPathStyleAccess() { return pathStyleAccess; }
    public void setPathStyleAccess(boolean pathStyleAccess) { this.pathStyleAccess = pathStyleAccess; }
}
