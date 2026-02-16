import { makeAutoObservable, runInAction } from 'mobx';
import type { UserDto } from '../api/models/userDto.ts';
import { AuthService } from '../api/services/authService.ts';

export class UserStore {
  private _user!: UserDto;

  constructor() {
    makeAutoObservable(this, {}, { autoBind: true });
  }

  get user() {
    return this._user;
  }

  set user(value: UserDto) {
    this._user = value;
  }

  async fetchUser() {
    const user = await AuthService.getMe();
    runInAction(() => {
      this.user = user;
    })
  }
}