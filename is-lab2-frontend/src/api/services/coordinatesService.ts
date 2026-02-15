import { api } from '../api';
import type {
  CoordinatesDto,
  CoordinatesField,
  CoordinatesWithoutIdDto,
} from '../models/coordinates';
import type { PageDto } from '../models/pageDto';
import type { SortDirection } from '../models/sortDirection';

export class CoordinatesService {
  static async list(params?: {
    field?: CoordinatesField;
    value?: string;
    orderBy?: SortDirection;
    limit?: number;
    offset?: number;
  }) {
    const res = await api.get<PageDto<CoordinatesDto>>('/coordinates', {
      params,
    });
    return res.data;
  }

  static async create(dto: CoordinatesWithoutIdDto) {
    const res = await api.post<CoordinatesDto>('/coordinates', dto);
    return res.data;
  }

  static async get(id: number) {
    const res = await api.get<CoordinatesDto>(`/coordinates/${id}`);
    return res.data;
  }

  static async update(id: number, dto: CoordinatesDto) {
    const res = await api.put<CoordinatesDto>(`/coordinates/${id}`, dto);
    return res.data;
  }

  static async delete(id: number) {
    const res = await api.delete(`/coordinates/${id}`);
    return res.data;
  }
}
