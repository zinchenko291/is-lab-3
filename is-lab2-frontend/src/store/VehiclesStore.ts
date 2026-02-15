import { makeAutoObservable, runInAction } from 'mobx';
import type {
  VehicleDto,
  VehicleWithoutIdDto,
  VehicleField,
  FuelType,
} from '../api/models/vehicles';
import { VehiclesService } from '../api/services/vehiclesService';
import { getApiErrorMessage } from '../api/getApiErrorMessage';
import { RootStore } from './RootStore';
import type { SortDirection } from '../api/models/sortDirection';

export interface VehiclesFilter {
  field?: VehicleField;
  value?: string;
  orderBy?: SortDirection;
}

export class VehiclesStore {
  root: RootStore;

  vehicles: VehicleDto[] = [];
  total = 0;

  loading = false;
  error: string | null = null;

  pageSize = 10;
  currentPage = 0;

  filters: VehiclesFilter = {};

  constructor(root: RootStore) {
    this.root = root;
    makeAutoObservable(this);
  }

  async loadVehicles(pageIndex = 0) {
    this.loading = true;
    this.error = null;

    const limit = this.pageSize;
    const offset = pageIndex;

    try {
      const data = await VehiclesService.list({
        ...this.filters,
        limit,
        offset,
      });

      runInAction(() => {
        this.vehicles = data.items;
        this.total = data.total;
        this.currentPage = pageIndex;
      });
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      runInAction(() => {
        this.error = message ?? 'Ошибка загрузки';
      });
    } finally {
      runInAction(() => {
        this.loading = false;
      });
    }
  }

  setPageSize(size: number) {
    this.pageSize = size;
    this.loadVehicles(0);
  }

  setFilters(filters: VehiclesFilter) {
    this.filters = filters;
    this.loadVehicles(0);
  }

  get totalPages(): number {
    if (this.pageSize <= 0) return 0;
    return Math.ceil(this.total / this.pageSize);
  }

  get hasPrevPage(): boolean {
    return this.currentPage > 0;
  }

  get hasNextPage(): boolean {
    return this.currentPage + 1 < this.totalPages;
  }

  async goToPage(pageIndex: number) {
    if (pageIndex < 0 || (this.totalPages && pageIndex >= this.totalPages))
      return;
    await this.loadVehicles(pageIndex);
  }

  async createVehicle(dto: VehicleWithoutIdDto) {
    const created = await VehiclesService.create(dto);
    await this.loadVehicles(0);
    return created;
  }

  async updateVehicle(id: number, dto: VehicleDto) {
    const updated = await VehiclesService.update(id, dto);
    runInAction(() => {
      const idx = this.vehicles.findIndex((v) => v.id === id);
      if (idx >= 0) this.vehicles[idx] = updated;
    });
    return updated;
  }

  async deleteVehicle(id: number) {
    await VehiclesService.delete(id);
    const page = this.currentPage;
    await this.loadVehicles(page > 0 ? page : 0);
  }

  getById(id: number) {
    return this.vehicles.find((v) => v.id === id) ?? null;
  }

  async fetchById(id: number) {
      this.loading = true;
      this.error = null;
  
      try {
        const data = await VehiclesService.get(id);
  
        runInAction(() => {
          const existingIndex = this.vehicles.findIndex((c) => c.id === id);
          if (existingIndex >= 0) {
            this.vehicles[existingIndex] = data;
          } else {
            this.vehicles.push(data);
            this.total += 1;
          }
        });
  
        return data;
      } catch (e: any) {
        const message = await getApiErrorMessage(e);
        runInAction(() => {
          this.error =
            message ?? 'Не удалось загрузить траспортное средство';
        });
        throw e;
      } finally {
        runInAction(() => {
          this.loading = false;
        });
      }
    }

  async loadMinEnginePower() {
    return await VehiclesService.minEnginePower();
  }

  async countGreaterFuel(fuel: FuelType) {
    return await VehiclesService.countGreaterFuelType(fuel);
  }

  async searchByName(name: string, offset = 0, limit = 50) {
    return await VehiclesService.searchByName({ name, offset, limit });
  }

  async findByEnginePowerRange(min: number, max: number, offset = 0, limit = 50) {
    return await VehiclesService.findByEnginePowerRange({min, max, offset, limit});
  }

  async resetDistanceTravelled(id: number) {
    return await VehiclesService.resetDistanceTravelled(id);
  }
}


