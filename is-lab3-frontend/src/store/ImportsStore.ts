import { makeAutoObservable, runInAction } from 'mobx';
import type {
  ImportConflict,
  ImportOperation,
  ImportResolveAction,
  ImportUploadFormat,
} from '../api/models/imports';
import { ImportsService } from '../api/services/importsService';
import { getApiErrorMessage } from '../api/getApiErrorMessage';
import type { RootStore } from './RootStore';

type WsAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'STATUS' | 'CONFLICT';

export class ImportsStore {
  root: RootStore;

  operations: ImportOperation[] = [];
  conflicts: ImportConflict[] = [];
  currentImportId: number | null = null;

  historyLoading = false;
  conflictsLoading = false;
  startLoading = false;

  historyError: string | null = null;
  conflictsError: string | null = null;
  startError: string | null = null;

  constructor(root: RootStore) {
    this.root = root;
    makeAutoObservable(this);
  }

  async loadHistory() {
    this.historyLoading = true;
    this.historyError = null;

    try {
      const data = await ImportsService.listHistory();
      runInAction(() => {
        this.operations = data;
      });
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      runInAction(() => {
        this.historyError =
          message ?? 'Ошибка загрузки импортов';
      });
    } finally {
      runInAction(() => {
        this.historyLoading = false;
      });
    }
  }

  async startImport(file: File, format?: ImportUploadFormat) {
    this.startLoading = true;
    this.startError = null;

    try {
      const operation = await ImportsService.startImport({ file, format });
      runInAction(() => {
        const existingIndex = this.operations.findIndex(
          (item) => item.id === operation.id
        );
        if (existingIndex >= 0) {
          this.operations[existingIndex] = operation;
        } else {
          this.operations.unshift(operation);
        }
      });
      return operation;
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      runInAction(() => {
        this.startError =
          message ?? 'Не удалось запустить импорт';
      });
      throw e;
    } finally {
      runInAction(() => {
        this.startLoading = false;
      });
    }
  }

  async loadConflicts(importId: number) {
    this.conflictsLoading = true;
    this.conflictsError = null;
    this.currentImportId = importId;
    this.conflicts = [];

    try {
      const data = await ImportsService.listConflicts(importId);
      runInAction(() => {
        this.conflicts = data;
      });
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      runInAction(() => {
        this.conflictsError =
          message ?? 'Ошибка загрузки конфликтов';
      });
    } finally {
      runInAction(() => {
        this.conflictsLoading = false;
      });
    }
  }

  clearConflicts() {
    this.conflicts = [];
    this.currentImportId = null;
    this.conflictsError = null;
  }

  async resolveConflict(
    importId: number,
    conflictId: number,
    resolution: ImportResolveAction
  ) {
    const operation = await ImportsService.resolveConflict({
      id: importId,
      conflictId,
      resolution,
    });

    runInAction(() => {
      const opIndex = this.operations.findIndex(
        (item) => item.id === operation.id
      );
      if (opIndex >= 0) this.operations[opIndex] = operation;

      const conflictIndex = this.conflicts.findIndex(
        (item) => item.id === conflictId
      );
      if (conflictIndex >= 0) {
        this.conflicts[conflictIndex] = {
          ...this.conflicts[conflictIndex],
          resolution,
        };
      }
    });

    return operation;
  }

  getOperationById(id: number) {
    return this.operations.find((item) => item.id === id) ?? null;
  }

  applyImportEvent(action: WsAction, id: number, payload: ImportOperation) {
    const index = this.operations.findIndex((item) => item.id === id);

    const actionHandlers: Record<WsAction, () => void> = {
      CREATE: () => {
        if (index >= 0) {
          this.operations[index] = payload;
        } else {
          this.operations.unshift(payload);
        }
      },
      UPDATE: () => {
        if (index < 0) return;
        this.operations[index] = payload;
      },
      STATUS: () => {
        if (index < 0) {
          this.operations.unshift(payload);
          return;
        }
        this.operations[index] = payload;
      },
      CONFLICT: () => {
        if (index < 0) {
          this.operations.unshift(payload);
          return;
        }
        this.operations[index] = payload;
      },
      DELETE: () => {
        if (index < 0) return;
        this.operations.splice(index, 1);
      },
    };

    actionHandlers[action]?.();
  }

  applyConflictEvent(
    action: WsAction,
    payload: ImportConflict,
    importId: number | null
  ) {
    if (importId === null || this.currentImportId !== importId) return;

    const index = this.conflicts.findIndex((item) => item.id === payload.id);

    const actionHandlers: Record<WsAction, () => void> = {
      CREATE: () => {
        if (index >= 0) {
          this.conflicts[index] = payload;
        } else {
          this.conflicts.unshift(payload);
        }
      },
      UPDATE: () => {
        if (index < 0) return;
        this.conflicts[index] = payload;
      },
      STATUS: () => {
        if (index < 0) return;
        this.conflicts[index] = payload;
      },
      CONFLICT: () => {
        if (index >= 0) {
          this.conflicts[index] = payload;
        } else {
          this.conflicts.unshift(payload);
        }
      },
      DELETE: () => {
        if (index < 0) return;
        this.conflicts.splice(index, 1);
      },
    };

    actionHandlers[action]?.();
  }
}

