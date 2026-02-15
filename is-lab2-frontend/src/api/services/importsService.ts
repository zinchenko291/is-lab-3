import { api } from '../api';
import type {
  ImportConflict,
  ImportOperation,
  ImportResolveAction,
  ImportUploadFormat,
} from '../models/imports';

export class ImportsService {
  static async startImport(params: {
    file: File;
    format?: ImportUploadFormat;
  }) {
    const formData = new FormData();
    formData.append('file', params.file);

    const res = await api.post<ImportOperation>('/imports', formData, {
      params: params.format ? { format: params.format } : undefined,
      headers: { 'Content-Type': 'multipart/form-data' },
    });

    return res.data;
  }

  static async listHistory() {
    const res = await api.get<ImportOperation[]>('/imports');
    return res.data;
  }

  static async listConflicts(id: number) {
    const res = await api.get<ImportConflict[]>(`/imports/${id}/conflicts`);
    return res.data;
  }

  static async resolveConflict(params: {
    id: number;
    conflictId: number;
    resolution: ImportResolveAction;
  }) {
    const res = await api.post<ImportOperation>(
      `/imports/${params.id}/conflicts/${params.conflictId}/resolve`,
      null,
      {
        params: { resolution: params.resolution },
      }
    );
    return res.data;
  }
}
