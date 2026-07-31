package com.aiqaos.execution.artifact;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * ENT-5 (ADR-068): the durable {@link ObjectStorageClient}, backed by the S3 API (MinIO today, real
 * AWS S3 / any S3-compatible store later). Active only when object storage is selected and the S3
 * provider is chosen — see {@link InMemoryObjectStorageClient} (the default, guarded by the inverse
 * expression) so exactly one client bean is active regardless of component-scan order.
 *
 * <p>Preserves the seam's contract: a missing key surfaces as {@link NoSuchElementException} (as the
 * in-memory reference does), {@code exists} is a HEAD probe, and {@code delete} is idempotent.
 */
@Component
@ConditionalOnClass(S3Client.class)
@ConditionalOnExpression(
        "'${aiqaos.artifacts.store:}' == 'object' and '${aiqaos.artifacts.object.provider:in-memory}' == 's3'")
public class S3ObjectStorageClient implements ObjectStorageClient {

    private final S3Client s3;
    private final String bucket;

    public S3ObjectStorageClient(S3Client s3, S3StorageProperties props) {
        this.s3 = s3;
        this.bucket = props.getBucket();
    }

    @Override
    public void put(String key, byte[] content) {
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(),
                RequestBody.fromBytes(content));
    }

    @Override
    public byte[] get(String key) {
        try {
            ResponseBytes<GetObjectResponse> bytes = s3.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(key).build());
            return bytes.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new NoSuchElementException("No object: " + key);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public List<String> list(String prefix) {
        List<String> keys = new ArrayList<>();
        ListObjectsV2Request.Builder request = ListObjectsV2Request.builder().bucket(bucket);
        if (prefix != null && !prefix.isEmpty()) {
            request.prefix(prefix);
        }
        String continuationToken = null;
        do {
            if (continuationToken != null) {
                request.continuationToken(continuationToken);
            }
            ListObjectsV2Response response = s3.listObjectsV2(request.build());
            for (S3Object object : response.contents()) {
                keys.add(object.key());
            }
            continuationToken = Boolean.TRUE.equals(response.isTruncated()) ? response.nextContinuationToken() : null;
        } while (continuationToken != null);
        return keys;
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(builder -> builder.bucket(bucket).key(key));
    }

    @Override
    public Instant lastModified(String key) {
        try {
            HeadObjectResponse head = s3.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return head.lastModified();
        } catch (NoSuchKeyException e) {
            throw new NoSuchElementException("No object: " + key);
        }
    }
}
