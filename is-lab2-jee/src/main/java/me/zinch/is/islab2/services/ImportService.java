package me.zinch.is.islab2.services;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import me.zinch.is.islab2.exceptions.ForbiddenException;
import me.zinch.is.islab2.exceptions.ResourceNotFoundException;
import me.zinch.is.islab2.models.dao.implementations.ImportConflictDao;
import me.zinch.is.islab2.models.dao.implementations.ImportOperationDao;
import me.zinch.is.islab2.models.dto.imports.ImportConflictDto;
import me.zinch.is.islab2.models.dto.imports.ImportConflictMapper;
import me.zinch.is.islab2.models.dto.imports.ImportOperationDto;
import me.zinch.is.islab2.models.dto.imports.ImportOperationMapper;
import me.zinch.is.islab2.models.entities.ImportConflict;
import me.zinch.is.islab2.models.entities.ImportConflictResolution;
import me.zinch.is.islab2.models.entities.ImportFormat;
import me.zinch.is.islab2.models.entities.ImportOperation;
import me.zinch.is.islab2.models.entities.ImportStatus;
import me.zinch.is.islab2.models.entities.User;
import me.zinch.is.islab2.models.events.ws.WsEvent;
import me.zinch.is.islab2.models.ws.WsAction;
import me.zinch.is.islab2.models.ws.WsEntity;

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
    private ImportPayloadCache payloadCache;
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
                         ImportPayloadCache payloadCache,
                         MailService mailService,
                         Event<WsEvent> wsEvent) {
        this.importOperationDao = importOperationDao;
        this.importConflictDao = importConflictDao;
        this.importOperationMapper = importOperationMapper;
        this.importConflictMapper = importConflictMapper;
        this.importProcessor = importProcessor;
        this.payloadCache = payloadCache;
        this.mailService = mailService;
        this.wsEvent = wsEvent;
    }

    public ImportOperationDto startImport(User user, ImportFormat format, String payload) {
        ImportOperation operation = self.createOperation(user, format);
        payloadCache.put(operation.getId(), payload);
        ImportOperationDto dto = importOperationMapper.entityToDto(operation);
        wsEvent.fire(new WsEvent(WsEntity.IMPORT_OPERATION, WsAction.STATUS, operation.getId(), dto));
        submitImport(operation.getId(), payload, null);
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
            String payload = payloadCache.get(operation.getId()).orElse(null);
            if (payload == null) {
                operation.setStatus(ImportStatus.FAILED);
                operation.setErrorMessage("Файл импорта недоступен (истек период хранения)");
                operation.setCompletedAt(new Date());
                self.updateOperation(operation);
                ImportOperationDto dto = importOperationMapper.entityToDto(operation);
                wsEvent.fire(new WsEvent(WsEntity.IMPORT_OPERATION, WsAction.STATUS, operation.getId(), dto));
                return dto;
            }
            operation.setStatus(ImportStatus.RUNNING);
            operation = self.updateOperation(operation);
            submitImport(operation.getId(), payload, loadResolutions(operation.getId()));
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
    public ImportOperation createOperation(User user, ImportFormat format) {
        ImportOperation operation = new ImportOperation();
        operation.setUser(user);
        operation.setFormat(format);
        operation.setStatus(ImportStatus.RUNNING);
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
    private void submitImport(Integer operationId, String payload, Map<Integer, ImportConflictResolution> resolutions) {
        if (executor == null) {
            runImport(operationId, payload, resolutions);
            return;
        }
        executor.submit(() -> runImport(operationId, payload, resolutions));
    }

    private void runImport(Integer operationId, String payload, Map<Integer, ImportConflictResolution> resolutions) {
        ImportOperation operation = importOperationDao.findById(operationId).orElse(null);
        if (operation == null) {
            return;
        }
        try {
            ImportProcessor.ImportResult result = importProcessor.processImport(operation, payload, resolutions);
            if (!result.conflicts().isEmpty()) {
                operation.setStatus(ImportStatus.PAUSED);
                operation = self.updateOperation(operation);
                self.saveConflicts(operation, result.conflicts());
            } else {
                operation.setStatus(ImportStatus.SUCCEEDED);
                operation.setAddedCount(result.added());
                operation.setCompletedAt(new Date());
                operation = self.updateOperation(operation);
                payloadCache.remove(operation.getId());
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Import failed for operation id=" + operation.getId(), e);
            operation.setStatus(ImportStatus.FAILED);
            operation.setErrorMessage(buildErrorMessage(e));
            operation.setCompletedAt(new Date());
            operation = self.updateOperation(operation);
            payloadCache.remove(operation.getId());
        }

        ImportOperationDto dto = importOperationMapper.entityToDto(operation);
        wsEvent.fire(new WsEvent(WsEntity.IMPORT_OPERATION, WsAction.STATUS, operation.getId(), dto));
    }
}
