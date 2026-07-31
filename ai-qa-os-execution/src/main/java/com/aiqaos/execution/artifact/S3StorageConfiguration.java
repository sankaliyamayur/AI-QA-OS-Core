package com.aiqaos.execution.artifact;

import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * ENT-5 (ADR-068): builds the {@link S3Client} for the durable object-storage binding, active only
 * when object storage is selected AND the S3 provider is chosen — the same guard as
 * {@link S3ObjectStorageClient}, so the client and its config appear together or not at all.
 *
 * <p>Uses static credentials from {@link S3StorageProperties} (SEC-2: env/secret-injected),
 * an optional {@code endpointOverride} for MinIO / S3-compatible stores, path-style addressing
 * (MinIO requires it), and the minimal JDK-based {@link UrlConnectionHttpClient} sync HTTP client.
 */
@Configuration
@ConditionalOnClass(S3Client.class)
@ConditionalOnExpression(
        "'${aiqaos.artifacts.store:}' == 'object' and '${aiqaos.artifacts.object.provider:in-memory}' == 's3'")
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageConfiguration {

    @Bean
    public S3Client s3Client(S3StorageProperties props) {
        var builder = S3Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(props.isPathStyleAccess())
                        .build())
                .httpClient(UrlConnectionHttpClient.create());
        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }
        return builder.build();
    }
}
