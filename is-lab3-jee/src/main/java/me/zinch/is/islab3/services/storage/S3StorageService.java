package me.zinch.is.islab3.services.storage;

public interface S3StorageService {
    String uploadStaging(Integer operationId, String fileName, String contentType, byte[] content);

    String prepareCommitted(Integer operationId, String stagingKey, String fileName, String contentType);

    void commitPrepared(Integer operationId, String stagingKey);

    void rollbackPrepared(Integer operationId, String committedKey);

    S3StoredFile download(String key, String fallbackFileName);
}
