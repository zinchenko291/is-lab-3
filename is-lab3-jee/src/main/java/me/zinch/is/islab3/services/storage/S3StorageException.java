package me.zinch.is.islab3.services.storage;

public class S3StorageException extends RuntimeException {
    public S3StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
