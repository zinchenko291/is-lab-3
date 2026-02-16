import { api } from '../api';
import type {
  ImportConflict,
  ImportOperation,
  ImportResolveAction,
  ImportUploadFormat,
} from '../models/imports';

export class ImportsService {
  private static getFileNameFromDisposition(
    contentDisposition?: string
  ): string | null {
    if (!contentDisposition) {
      return null;
    }

    const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8Match?.[1]) {
      return decodeURIComponent(utf8Match[1]);
    }

    const plainMatch = contentDisposition.match(/filename="?([^"]+)"?/i);
    return plainMatch?.[1] ?? null;
  }

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

  static async downloadFile(params: {
    id: number
  }) {
    const res = await api.get<Blob>(`/imports/${params.id}/file`, {
      responseType: 'blob',
    });
    console.log(res);
    
    return {
      blob: res.data,
      fileName: this.getFileNameFromDisposition(
        res.headers['content-disposition']
      ),
    };
  }
}
