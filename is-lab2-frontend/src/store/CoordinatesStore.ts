import { makeAutoObservable, runInAction } from 'mobx';
import type {
  CoordinatesDto,
  CoordinatesWithoutIdDto,
  CoordinatesField,
} from '../api/models/coordinates';
import type { SortDirection } from '../api/models/sortDirection';
import { CoordinatesService } from '../api/services/coordinatesService';
import { getApiErrorMessage } from '../api/getApiErrorMessage';
import { RootStore } from './RootStore';

export interface CoordinatesFilters {
  field?: CoordinatesField;
  value?: string;
  orderBy?: SortDirection;
}

export class CoordinatesStore {
  root: RootStore;

  coordinates: CoordinatesDto[] = [];
  total = 0;

  loading = false;
  error: string | null = null;

  pageSize = 10;
  currentPage = 0;

  filters: CoordinatesFilters = {};

  constructor(root: RootStore) {
    this.root = root;
    makeAutoObservable(this);
  }

  async loadCoordinates(pageIndex = 0) {
    this.loading = true;
    this.error = null;

    const limit = this.pageSize;
    const offset = pageIndex;

    try {
      const page = await CoordinatesService.list({
        ...this.filters,
        limit,
        offset,
      });

      runInAction(() => {
        this.coordinates = page.items;
        this.total = page.total;
        this.currentPage = pageIndex;
      });
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      runInAction(() => {
        this.error = message ?? 'Ошибка загрузки координат';
      });
    } finally {
      runInAction(() => {
        this.loading = false;
      });
    }
  }

  setPageSize(size: number) {
    this.pageSize = size;
    this.loadCoordinates(0);
  }

  setFilters(filters: CoordinatesFilters) {
    this.filters = filters;
    this.loadCoordinates(0);
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
    if (pageIndex < 0) return;
    if (this.totalPages && pageIndex >= this.totalPages) return;
    await this.loadCoordinates(pageIndex);
  }

  getById(id: number) {
    return this.coordinates.find((c) => c.id === id) ?? null;
  }

  async fetchById(id: number) {
    this.loading = true;
    this.error = null;

    try {
      const data = await CoordinatesService.get(id);

      runInAction(() => {
        const existingIndex = this.coordinates.findIndex((c) => c.id === id);
        if (existingIndex >= 0) {
          this.coordinates[existingIndex] = data;
        } else {
          this.coordinates.push(data);
          this.total += 1;
        }
      });

      return data;
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      runInAction(() => {
        this.error =
          message ?? 'Не удалось загрузить координату';
      });
      throw e;
    } finally {
      runInAction(() => {
        this.loading = false;
      });
    }
  }

  async createCoordinates(dto: CoordinatesWithoutIdDto) {
    const created = await CoordinatesService.create(dto);

    await this.loadCoordinates(0);

    return created;
  }

  async updateCoordinates(id: number, dto: CoordinatesDto) {
    const updated = await CoordinatesService.update(id, dto);

    runInAction(() => {
      const i = this.coordinates.findIndex((c) => c.id === id);
      if (i >= 0) this.coordinates[i] = updated;
    });

    return updated;
  }

  async deleteCoordinates(id: number) {
    await CoordinatesService.delete(id);

    const page = this.currentPage;
    await this.loadCoordinates(page > 0 ? page : 0);
  }
}

