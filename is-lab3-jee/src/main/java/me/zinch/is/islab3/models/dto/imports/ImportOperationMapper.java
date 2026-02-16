package me.zinch.is.islab3.models.dto.imports;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import me.zinch.is.islab3.models.dto.user.UserMapper;
import me.zinch.is.islab3.models.entities.ImportOperation;
import me.zinch.is.islab3.models.entities.ImportStatus;

@ApplicationScoped
public class ImportOperationMapper {
    private UserMapper userMapper;

    public ImportOperationMapper() {}

    @Inject
    public ImportOperationMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ImportOperationDto entityToDto(ImportOperation operation) {
        if (operation == null) return null;
        ImportOperationDto dto = new ImportOperationDto();
        dto.setId(operation.getId());
        dto.setStatus(operation.getStatus());
        dto.setFormat(operation.getFormat());
        dto.setStartedAt(operation.getStartedAt());
        dto.setCompletedAt(operation.getCompletedAt());
        if (operation.getStatus() == null || operation.getStatus() == ImportStatus.SUCCEEDED) {
            dto.setAddedCount(operation.getAddedCount());
        }
        dto.setErrorMessage(operation.getErrorMessage());
        dto.setSourceFileName(operation.getSourceFileName());
        dto.setSourceFileSize(operation.getSourceFileSize());
        dto.setFileAvailable(operation.getS3ObjectKey() != null && !operation.getS3ObjectKey().isBlank());
        dto.setUser(userMapper == null ? null : userMapper.entityToShortDto(operation.getUser()));
        return dto;
    }
}
