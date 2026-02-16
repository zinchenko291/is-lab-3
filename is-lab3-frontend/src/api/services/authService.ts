import { api } from '../api.ts';
import type { ChallengeResponse } from '../models/challengeResponse.ts';
import type { UserDto } from '../models/userDto.ts';
import { AxiosError } from 'axios';

export class AuthService {
  static async register(params: {
    name: string;
    pubkey: string;
    email: string;
  }) {
    const res = await api.post<ChallengeResponse>('/auth/register', params);
    return res.data;
  }

  static async registerVerify(params: {
    pubkey: string;
    signature: string;
  }) {
    const res = await api.post<UserDto>('/auth/register/verify', params);
    return res.data;
  }

  static async login(params: {
    pubkey: string;
  }) {
    const res = await api.post<ChallengeResponse>('/auth/login', params);
    return res.data;
  }

  static async loginVerify(params: {
    pubkey: string;
    signature: string;
  }) {
    const res = await api.post<UserDto>('/auth/login/verify', params);
    return res.data;
  }

  static async validate(): Promise<boolean> {
    try {
      await api.get('/auth/validate');
      return true;
    } catch (error) {
      if (error instanceof AxiosError && error.status === 401) return false;
      throw error;
    }
  }

  static async getMe(): Promise<UserDto> {
    const res = await api.get<UserDto>('/auth/me');
    return res.data;
  }

  static async logout(): Promise<void> {
    await api.get('/auth/logout');
  }
}