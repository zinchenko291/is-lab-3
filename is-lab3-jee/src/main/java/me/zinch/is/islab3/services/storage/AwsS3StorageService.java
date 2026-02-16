package me.zinch.is.islab3.services.storage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import me.zinch.is.islab3.Config;
import me.zinch.is.islab3.exceptions.StorageUnavailableException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@ApplicationScoped
public class AwsS3StorageService implements S3StorageService {
    private S3Client s3;
    private volatile boolean bucketReady;

    @PostConstruct
    public void init() {
        s3 = S3Client.builder()
                .endpointOverride(URI.create(Config.S3_ENDPOINT))
                .region(Region.of(Config.S3_REGION))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(Config.S3_ACCESS_KEY, Config.S3_SECRET_KEY)
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .build();
    }

    @Override
    public String uploadStaging(Integer operationId, String fileName, String contentType, byte[] content) {
        ensureBucketExists();
        String safeFileName = sanitizeFileName(fileName);
        String key = "imports/staging/op-" + operationId + "/" + UUID.randomUUID() + "-" + safeFileName;
        putObject(key, contentType, content);
        return key;
    }

    @Override
    public String prepareCommitted(Integer operationId, String stagingKey, String fileName, String contentType) {
        ensureBucketExists();
        String safeFileName = sanitizeFileName(fileName);
        String committedKey = "imports/committed/op-" + operationId + "/" + safeFileName;
        try {
            s3.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(Config.S3_BUCKET)
                    .sourceKey(stagingKey)
                    .destinationBucket(Config.S3_BUCKET)
                    .destinationKey(committedKey)
                    .contentType(contentType)
                    .metadataDirective("REPLACE")
                    .build());
            return committedKey;
        } catch (Exception e) {
            throw new StorageUnavailableException("S3 недоступно: не удалось подготовить файл импорта", e);
        }
    }

    @Override
    public void commitPrepared(Integer operationId, String stagingKey) {
        if (stagingKey == null || stagingKey.isBlank()) {
            return;
        }
        deleteQuiet(stagingKey);
    }

    @Override
    public void rollbackPrepared(Integer operationId, String committedKey) {
        if (committedKey == null || committedKey.isBlank()) {
            return;
        }
        deleteQuiet(committedKey);
    }

    @Override
    public S3StoredFile download(String key, String fallbackFileName) {
        try (ResponseInputStream<GetObjectResponse> in = s3.getObject(GetObjectRequest.builder()
                .bucket(Config.S3_BUCKET)
                .key(key)
                .build())) {
            byte[] bytes = in.readAllBytes();
            String fileName = fallbackFileName == null || fallbackFileName.isBlank()
                    ? extractFileName(key)
                    : fallbackFileName;
            String contentType = in.response().contentType();
            return new S3StoredFile(key, fileName, contentType, bytes);
        } catch (IOException e) {
            throw new StorageUnavailableException("S3 недоступно: не удалось прочитать файл импорта", e);
        } catch (Exception e) {
            throw new StorageUnavailableException("S3 недоступно: не удалось скачать файл импорта", e);
        }
    }

    private void putObject(String key, String contentType, byte[] content) {
        try {
            s3.putObject(PutObjectRequest.builder()
                            .bucket(Config.S3_BUCKET)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content));
        } catch (Exception e) {
            throw new StorageUnavailableException("S3 недоступно: не удалось загрузить файл импорта", e);
        }
    }

    private void deleteQuiet(String key) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(Config.S3_BUCKET)
                    .key(key)
                    .build());
        } catch (Exception ignored) {
        }
    }

    private void ensureBucketExists() {
        if (bucketReady) {
            return;
        }
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(Config.S3_BUCKET).build());
            bucketReady = true;
        } catch (NoSuchBucketException e) {
            try {
                s3.createBucket(CreateBucketRequest.builder().bucket(Config.S3_BUCKET).build());
                bucketReady = true;
            } catch (Exception createError) {
                throw new StorageUnavailableException("S3 недоступно: не удалось создать bucket", createError);
            }
        } catch (Exception e) {
            if (!bucketExistsByListing()) {
                try {
                    s3.createBucket(CreateBucketRequest.builder().bucket(Config.S3_BUCKET).build());
                    bucketReady = true;
                } catch (Exception createError) {
                    throw new StorageUnavailableException("S3 недоступно: не удалось создать bucket", createError);
                }
            } else {
                bucketReady = true;
            }
        }
    }

    private boolean bucketExistsByListing() {
        try {
            ListObjectsV2Iterable pages = s3.listObjectsV2Paginator(b -> b.bucket(Config.S3_BUCKET).maxKeys(1));
            for (var ignored : pages) {
                return true;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "import.dat";
        }
        return fileName.replace("\\", "_").replace("/", "_");
    }

    private String extractFileName(String key) {
        int idx = key.lastIndexOf('/');
        if (idx < 0 || idx == key.length() - 1) {
            return "import.dat";
        }
        return key.substring(idx + 1);
    }
}
