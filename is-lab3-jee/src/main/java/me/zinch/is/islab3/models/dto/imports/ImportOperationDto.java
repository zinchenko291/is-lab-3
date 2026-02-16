package me.zinch.is.islab3.models.dto.imports;

import me.zinch.is.islab3.models.dto.user.UserShortDto;
import me.zinch.is.islab3.models.entities.ImportFormat;
import me.zinch.is.islab3.models.entities.ImportStatus;

import java.util.Date;

public class ImportOperationDto {
    private int id;
    private ImportStatus status;
    private ImportFormat format;
    private Date startedAt;
    private Date completedAt;
    private Integer addedCount;
    private String errorMessage;
    private String sourceFileName;
    private Long sourceFileSize;
    private boolean fileAvailable;
    private UserShortDto user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public void setStatus(ImportStatus status) {
        this.status = status;
    }

    public ImportFormat getFormat() {
        return format;
    }

    public void setFormat(ImportFormat format) {
        this.format = format;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(Integer addedCount) {
        this.addedCount = addedCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public Long getSourceFileSize() {
        return sourceFileSize;
    }

    public void setSourceFileSize(Long sourceFileSize) {
        this.sourceFileSize = sourceFileSize;
    }

    public boolean isFileAvailable() {
        return fileAvailable;
    }

    public void setFileAvailable(boolean fileAvailable) {
        this.fileAvailable = fileAvailable;
    }

    public UserShortDto getUser() {
        return user;
    }

    public void setUser(UserShortDto user) {
        this.user = user;
    }
}
