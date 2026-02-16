package me.zinch.is.islab3.services.imports;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import me.zinch.is.islab3.exceptions.ForbiddenException;
import me.zinch.is.islab3.exceptions.ResourceNotFoundException;
import me.zinch.is.islab3.models.dao.implementations.ImportConflictDao;
import me.zinch.is.islab3.models.dao.implementations.ImportOperationDao;
import me.zinch.is.islab3.models.dto.imports.ImportConflictDto;
import me.zinch.is.islab3.models.dto.imports.ImportConflictMapper;
import me.zinch.is.islab3.models.dto.imports.ImportOperationDto;
import me.zinch.is.islab3.models.dto.imports.ImportOperationMapper;
import me.zinch.is.islab3.models.entities.*;
import me.zinch.is.islab3.models.events.ws.WsEvent;
import me.zinch.is.islab3.models.ws.WsAction;
import me.zinch.is.islab3.models.ws.WsEntity;
import me.zinch.is.islab3.services.MailService;
import me.zinch.is.islab3.services.storage.S3StorageService;
import me.zinch.is.islab3.services.storage.S3StoredFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class ImportService {
    private static final Logger LOGGER = Logger.getLogger(ImportService.class.getName());

    private ImportOperationDao importOperationDao;
    private ImportConflictDao importConflictDao;
    private ImportOperationMapper importOperationMapper;
    private ImportConflictMapper importConflictMapper;
    private ImportProcessor importProcessor;
    private S3StorageService s3StorageService;
    private ImportFileTransactionCoordinator fileTxCoordinator;
    private ImportFailureInjectionService failureInjectionService;
    private MailService mailService;
    private Event<WsEvent> wsEvent;
    private ImportService self;

    @Resource
    private ManagedExecutorService executor;

    public ImportService() { }

    @Inject
    public ImportService(ImportOperationDao importOperationDao,
                         ImportConflictDao importConflictDao,
                         ImportOperationMapper importOperationMapper,
                         ImportConflictMapper importConflictMapper,
                         ImportProcessor importProcessor,
                         S3StorageService s3StorageService,
                         ImportFileTransactionCoordinator fileTxCoordinator,
                         ImportFailureInjectionService failureInjectionService,
                         MailService mailService,
                         Event<WsEvent> wsEvent) {
        this.importOperationDao = importOperationDao;
        this.importConflictDao = importConflictDao;
        this.importOperationMapper = importOperationMapper;
        this.importConflictMapper = importConflictMapper;
        this.importProcessor = importProcessor;
        this.s3StorageService = s3StorageService;
        this.fileTxCoordinator = fileTxCoordinator;
        this.failureInjectionService = failureInjectionService;
        this.mailService = mailService;
        this.wsEvent = wsEvent;
    }

    public ImportOperationDto startImport(User user,
                                          ImportFormat format,
                                          String fileName,
                                          String contentType,
                                          byte[] payload) {
        ImportOperation operation = self.createOperation(user, format, fileName, contentType, (long) payload.length);
        try {
            String stagingKey = s3StorageService.uploadStaging(operation.getId(), fileName, contentType, payload);
            operation.setS3StagingKey(stagingKey);
            operation = self.updateOperation(operation);
        } catch (RuntimeException e) {
            operation.setStatus(ImportStatus.FAILED);
            operation.setErrorMessage(buildErrorMessage(e));
            operation.setCompletedAt(new Date());
            operation = self.updateOperation(operation);
            ImportOperationDto failedDto = importOperationMapper.entityToDto(operation);
            wsEvent.fire(new WsEvent(WsEntity.IMPORT_OPERATION, WsAction.STATUS, operation.getId(), failedDto));
            throw e;
        }
        ImportOperationDto dto = importOperationMapper.entityToDto(operation);
        wsEvent.fire(new WsEvent(WsEntity.IMPORT_OPERATION, WsAction.STATUS, operation.getId(), dto));
        submitImport(operation.getId(), null);
        return dto;
    }

    public ImportOperationDto resolveConflict(User user, Integer operationId, Integer conflictId, ImportConflictResolution resolution) {
        ImportOperation operation = getOperationForUser(user, operationId);
        ImportConflict conflict = self.getConflict(conflictId);
        if (conflict.getOperation().getId() != operation.getId()) {
            throw new ForbiddenException("Доступ запрещен");
        }
        self.updateConflictResolution(conflict, resolution);

        List<ImportConflict> unresolved = importConflictDao.findUnresolvedByOperation(operation.getId());
        if (unresolved.isEmpty()) {
            operation.setStatus(ImportStatus.RUNNING);
            operation = self.updateOperation(operation);
            submitImport(operation.getId(), loadResolutions(operation.getId()));
        }

        ImportOperationDto dto = importOperationMapper.entityToDto(operation);
        wsEvent.fire(new WsEvent(WsEntity.IMPORT_OPERATION, WsAction.STATUS, operation.getId(), dto));
        return dto;
    }

    public List<ImportOperationDto> getHistoryForUser(User user) {
        List<ImportOperation> operations = user.getIsAdmin()
                ? importOperationDao.findAll()
                : importOperationDao.findAllByUser(user.getId());
        return operations.stream()
                .map(importOperationMapper::entityToDto)
                .toList();
    }

    public List<ImportConflictDto> getConflictsForUser(User user, Integer operationId) {
        ImportOperation operation = getOperationForUser(user, operationId);
        return importConflictDao.findByOperation(operation.getId()).stream()
                .map(importConflictMapper::entityToDto)
                .toList();
    }

    public S3StoredFile downloadFileForUser(User user, Integer operationId) {
        ImportOperation operation = getOperationForUser(user, operationId);
        String key = operation.getS3ObjectKey();
        if (key == null || key.isBlank()) {
            throw new ResourceNotFoundException("Файл импорта недоступен");
        }
        return s3StorageService.download(key, operation.getSourceFileName());
    }

    public ImportFailureMode getFailureMode() {
        return failureInjectionService.getMode();
    }

    public void setFailureMode(ImportFailureMode mode) {
        failureInjectionService.setMode(mode);
    }

    private ImportOperation getOperationForUser(User user, Integer operationId) {
        ImportOperation operation = importOperationDao.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Операция импорта не найдена"));
        if (!user.getIsAdmin() && operation.getUser().getId() != user.getId()) {
            throw new ForbiddenException("Доступ запрещен");
        }
        return operation;
    }

    private Map<Integer, ImportConflictResolution> loadResolutions(Integer operationId) {
        return importConflictDao.findByOperation(operationId).stream()
                .collect(Collectors.toMap(ImportConflict::getVehicleIndex, ImportConflict::getResolution));
    }

    @Inject
    public void setSelf(ImportService self) {
        this.self = self;
    }

    @Transactional(TxType.REQUIRES_NEW)
    public ImportOperation createOperation(User user,
                                           ImportFormat format,
                                           String sourceFileName,
                                           String sourceContentType,
                                           Long sourceFileSize) {
        ImportOperation operation = new ImportOperation();
        operation.setUser(user);
        operation.setFormat(format);
        operation.setStatus(ImportStatus.RUNNING);
        operation.setSourceFileName(sourceFileName);
        operation.setSourceContentType(sourceContentType);
        operation.setSourceFileSize(sourceFileSize);
        return importOperationDao.create(operation);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public ImportOperation updateOperation(ImportOperation operation) {
        return importOperationDao.update(operation);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void failExpired(Integer operationId) {
        if (operationId == null) {
            return;
        }
        ImportOperation operation = importOperationDao.findById(operationId).orElse(null);
        if (operation == null) {
            return;
        }
        if (operation.getStatus() == ImportStatus.SUCCEEDED || operation.getStatus() == ImportStatus.FAILED) {
            return;
        }
        operation.setStatus(ImportStatus.FAILED);
        operation.setErrorMessage("Файл импорта недоступен (истек период хранения)");
        operation.setCompletedAt(new Date());
        updateOperation(operation);
        ImportOperationDto dto = importOperationMapper.entityToDto(operation);
        wsEvent.fire(new WsEvent(WsEntity.IMPORT_OPERATION, WsAction.STATUS, operation.getId(), dto));
    }

    @Transactional(TxType.REQUIRES_NEW)
    public ImportConflict getConflict(Integer conflictId) {
        return importConflictDao.findById(conflictId)
                .orElseThrow(() -> new ResourceNotFoundException("Конфликт не найден"));
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void updateConflictResolution(ImportConflict conflict, ImportConflictResolution resolution) {
        conflict.setResolution(resolution);
        importConflictDao.update(conflict);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void saveConflicts(ImportOperation operation, List<ImportProcessor.ImportConflictInfo> conflicts) {
        saveConflictsInternal(operation, conflicts);
    }

    private void saveConflictsInternal(ImportOperation operation, List<ImportProcessor.ImportConflictInfo> conflicts) {
        for (ImportProcessor.ImportConflictInfo conflictInfo : conflicts) {
            ImportConflict conflict = new ImportConflict();
            conflict.setOperation(operation);
            conflict.setResolution(ImportConflictResolution.UNRESOLVED);
            conflict.setVehicleIndex(conflictInfo.vehicleIndex());
            conflict.setExistingVehicleId(conflictInfo.existingVehicleId());
            conflict.setCoordinateX(conflictInfo.coordinateX());
            conflict.setCoordinateY(conflictInfo.coordinateY());
            conflict = importConflictDao.create(conflict);
            ImportConflictDto dto = importConflictMapper.entityToDto(conflict);
            wsEvent.fire(new WsEvent(WsEntity.IMPORT_CONFLICT, WsAction.CONFLICT, conflict.getId(), dto));
            mailService.sendConflictEmail(operation.getUser(), conflict);
        }
    }

    private String buildErrorMessage(RuntimeException e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            return e.getMessage();
        }
        return "Ошибка: " + e.getClass().getSimpleName();
    }

    private void submitImport(Integer operationId, Map<Integer, ImportConflictResolution> resolutions) {
        if (executor == null) {
            runImport(operationId, resolutions);
            return;
        }
        executor.submit(() -> runImport(operationId, resolutions));
    }

    private void runImport(Integer operationId, Map<Integer, ImportConflictResolution> resolutions) {
        ImportOperation operation = importOperationDao.findById(operationId).orElse(null);
        if (operation == null) {
            return;
        }
        boolean dbCompleted = false;
        try {
            operation = fileTxCoordinator.prepareFilePhase(operation);
            operation = self.updateOperation(operation);
            failureInjectionService.failIfConfigured(ImportFailureMode.AFTER_S3_PREPARE_BEFORE_DB);
            String payload = fileTxCoordinator.loadPayload(operation);
            ImportProcessor.ImportResult result = importProcessor.processImport(operation, payload, resolutions);
            if (!result.conflicts().isEmpty()) {
                dbCompleted = true;
                operation.setStatus(ImportStatus.PAUSED);
                operation = self.updateOperation(operation);
                self.saveConflicts(operation, result.conflicts());
            } else {
                dbCompleted = true;
                operation.setStatus(ImportStatus.SUCCEEDED);
                operation.setAddedCount(result.added());
                operation.setCompletedAt(new Date());
                operation = fileTxCoordinator.finishFileCommit(operation);
                operation = self.updateOperation(operation);
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Import failed for operation id=" + operation.getId(), e);
            if (!dbCompleted) {
                fileTxCoordinator.rollbackFilePhase(operation);
            }
            operation.setStatus(ImportStatus.FAILED);
            operation.setErrorMessage(buildErrorMessage(e));
            operation.setCompletedAt(new Date());
            operation = self.updateOperation(operation);
        }

        ImportOperationDto dto = importOperationMapper.entityToDto(operation);
        wsEvent.fire(new WsEvent(WsEntity.IMPORT_OPERATION, WsAction.STATUS, operation.getId(), dto));
    }
}
