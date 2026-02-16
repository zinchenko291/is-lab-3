import { api } from '../api';
import type { PageDto } from '../models/pageDto';
import type { SortDirection } from '../models/sortDirection';
import type {
  FuelType,
  VehicleDto,
  VehicleField,
  VehicleWithoutIdDto,
} from '../models/vehicles';

export class VehiclesService {
  static async list(params?: {
    field?: VehicleField;
    value?: string;
    orderBy?: SortDirection;
    limit?: number;
    offset?: number;
  }) {
    const res = await api.get<PageDto<VehicleDto>>('/vehicles', { params });
    return res.data;
  }

  static async create(dto: VehicleWithoutIdDto) {
    const res = await api.post<VehicleDto>('/vehicles', dto);
    return res.data;
  }

  static async get(id: number) {
    const res = await api.get<VehicleDto>(`/vehicles/${id}`);
    return res.data;
  }

  static async update(id: number, dto: VehicleDto) {
    const res = await api.put<VehicleDto>(`/vehicles/${id}`, dto);
    return res.data;
  }

  static async delete(id: number) {
    const res = await api.delete(`/vehicles/${id}`);
    return res.data;
  }

  static async minEnginePower() {
    const res = await api.get<VehicleDto>('/vehicles/min-engine-power');
    return res.data;
  }

  static async countGreaterFuelType(fuelType: FuelType) {
    const res = await api.get<number>('/vehicles/count-gt-fuel-type', {
      params: { fuelType },
    });
    return res.data;
  }

  static async searchByName(params: {
    name: string;
    offset?: number;
    limit?: number;
  }) {
    const res = await api.get<PageDto<VehicleDto>>('/vehicles/search-by-name', {
      params,
    });
    return res.data;
  }

  static async findByEnginePowerRange(params: {
    min: number;
    max: number;
    offset?: number;
    limit?: number;
  }) {
    const res = await api.get<PageDto<VehicleDto>>(
      '/vehicles/engine-power-range',
      { params }
    );
    return res.data;
  }

  static async resetDistanceTravelled(id: number) {
    const res = await api.post<VehicleDto>(`/vehicles/${id}/reset-distance`);
    return res.data;
  }
}
