package me.zinch.is.islab3.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import me.zinch.is.islab3.cache.InfinispanEclipseLinkCacheInterceptor;
import org.eclipse.persistence.annotations.CacheInterceptor;

import java.util.Date;

@Entity
@CacheInterceptor(InfinispanEclipseLinkCacheInterceptor.class)
@Table(name = "import_operations")
public class ImportOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @NotNull
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private ImportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private ImportFormat format;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startedAt;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;

    @Column
    private Integer addedCount;

    @Column
    private String errorMessage;

    @Column
    private String sourceFileName;

    @Column
    private String sourceContentType;

    @Column
    private Long sourceFileSize;

    @Column
    private String s3StagingKey;

    @Column
    private String s3ObjectKey;

    @PrePersist
    public void onCreate() {
        if (startedAt == null) {
            startedAt = new Date();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @NotNull User getUser() {
        return user;
    }

    public void setUser(@NotNull User user) {
        this.user = user;
    }

    public @NotNull ImportStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull ImportStatus status) {
        this.status = status;
    }

    public @NotNull ImportFormat getFormat() {
        return format;
    }

    public void setFormat(@NotNull ImportFormat format) {
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

    public String getSourceContentType() {
        return sourceContentType;
    }

    public void setSourceContentType(String sourceContentType) {
        this.sourceContentType = sourceContentType;
    }

    public Long getSourceFileSize() {
        return sourceFileSize;
    }

    public void setSourceFileSize(Long sourceFileSize) {
        this.sourceFileSize = sourceFileSize;
    }

    public String getS3StagingKey() {
        return s3StagingKey;
    }

    public void setS3StagingKey(String s3StagingKey) {
        this.s3StagingKey = s3StagingKey;
    }

    public String getS3ObjectKey() {
        return s3ObjectKey;
    }

    public void setS3ObjectKey(String s3ObjectKey) {
        this.s3ObjectKey = s3ObjectKey;
    }
}
