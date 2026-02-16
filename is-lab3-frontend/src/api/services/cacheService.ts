import { api } from "../api";
import type { L2CacheStatsSnapshot } from "../models/L2CacheStatsSnapshot";

export class CacheService {
  static async getStats() {
    const res = await api.get<L2CacheStatsSnapshot>(`/cache/l2/stats`);
    return res.data;
  }
}