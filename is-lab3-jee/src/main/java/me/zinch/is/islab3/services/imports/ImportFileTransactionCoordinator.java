package me.zinch.is.islab3.services.imports;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import me.zinch.is.islab3.exceptions.ResourceNotFoundException;
import me.zinch.is.islab3.models.entities.ImportOperation;
import me.zinch.is.islab3.services.storage.S3StorageService;
import me.zinch.is.islab3.services.storage.S3StoredFile;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class ImportFileTransactionCoordinator {
    private S3StorageService s3StorageService;

    public ImportFileTransactionCoordinator() { }

    @Inject
    public ImportFileTransactionCoordinator(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    public ImportOperation prepareFilePhase(ImportOperation operation) {
        if (operation.getS3ObjectKey() != null && !operation.getS3ObjectKey().isBlank()) {
            return operation;
        }
        String stagingKey = operation.getS3StagingKey();
        if (stagingKey == null || stagingKey.isBlank()) {
            throw new ResourceNotFoundException("Файл импорта недоступен в S3");
        }
        String committedKey = s3StorageService.prepareCommitted(
                operation.getId(),
                stagingKey,
                operation.getSourceFileName(),
                operation.getSourceContentType()
        );
        operation.setS3ObjectKey(committedKey);
        return operation;
    }

    public ImportOperation finishFileCommit(ImportOperation operation) {
        s3StorageService.commitPrepared(operation.getId(), operation.getS3StagingKey());
        operation.setS3StagingKey(null);
        return operation;
    }

    public void rollbackFilePhase(ImportOperation operation) {
        if (operation.getS3ObjectKey() != null && !operation.getS3ObjectKey().isBlank()) {
            s3StorageService.rollbackPrepared(operation.getId(), operation.getS3ObjectKey());
            operation.setS3ObjectKey(null);
        }
        if (operation.getS3StagingKey() != null && !operation.getS3StagingKey().isBlank()) {
            s3StorageService.commitPrepared(operation.getId(), operation.getS3StagingKey());
            operation.setS3StagingKey(null);
        }
    }

    public String loadPayload(ImportOperation operation) {
        String key = operation.getS3ObjectKey();
        if (key == null || key.isBlank()) {
            key = operation.getS3StagingKey();
        }
        if (key == null || key.isBlank()) {
            throw new ResourceNotFoundException("Файл импорта недоступен в S3");
        }
        S3StoredFile file = s3StorageService.download(key, operation.getSourceFileName());
        return new String(file.content(), StandardCharsets.UTF_8);
    }
}
