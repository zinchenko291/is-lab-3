export enum ImportStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  PAUSED = 'PAUSED',
  FAILED = 'FAILED',
  SUCCEEDED = 'SUCCEEDED',
}

export const importStatusLabels: Record<ImportStatus, string> = {
  [ImportStatus.PENDING]: 'Ожидает',
  [ImportStatus.RUNNING]: 'Выполняется',
  [ImportStatus.PAUSED]: 'Приостановлен',
  [ImportStatus.FAILED]: 'Ошибка',
  [ImportStatus.SUCCEEDED]: 'Успешно',
};

export enum ImportFormat {
  YAML = 'YAML',
  XML = 'XML',
}

export const importFormatLabels: Record<ImportFormat, string> = {
  [ImportFormat.YAML]: 'YAML',
  [ImportFormat.XML]: 'XML',
};

export enum ImportConflictResolution {
  UNRESOLVED = 'UNRESOLVED',
  SKIP = 'SKIP',
  OVERWRITE = 'OVERWRITE',
}

export const importConflictResolutionLabels: Record<
  ImportConflictResolution,
  string
> = {
  [ImportConflictResolution.UNRESOLVED]: 'Не обработан',
  [ImportConflictResolution.SKIP]: 'Пропущен',
  [ImportConflictResolution.OVERWRITE]: 'Перезаписан',
};

export interface UserShort {
  id: number;
  name: string;
}

export interface ImportOperation {
  id: number;
  status: ImportStatus;
  format: ImportFormat;
  startedAt: string;
  completedAt: string | null;
  addedCount: number | null;
  errorMessage: string | null;
  user: UserShort;
}

export interface ImportConflict {
  id: number;
  resolution: ImportConflictResolution;
  vehicleIndex: number;
  existingVehicleId: number | null;
  coordinateX: number;
  coordinateY: number;
  userId: number;
  createdAt: string;
  operationId?: number;
}

export type ImportUploadFormat = 'yaml' | 'yml' | 'xml';
export type ImportResolveAction = 'SKIP' | 'OVERWRITE';
