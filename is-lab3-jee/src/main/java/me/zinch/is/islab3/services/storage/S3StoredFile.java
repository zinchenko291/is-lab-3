package me.zinch.is.islab3.services.storage;

public record S3StoredFile(
        String key,
        String fileName,
        String contentType,
        byte[] content
) {
}
