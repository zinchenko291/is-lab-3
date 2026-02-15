import { VehiclesStore } from './VehiclesStore';
import { CoordinatesStore } from './CoordinatesStore';
import { WebSocketStore } from './WebSocketStore';
import { UserStore } from './UserStore.ts';
import { ImportsStore } from './ImportsStore';

export class RootStore {
  vehiclesStore: VehiclesStore;
  coordinatesStore: CoordinatesStore;
  webSocketStore: WebSocketStore;
  userStore: UserStore;
  importsStore: ImportsStore;

  constructor() {
    this.vehiclesStore = new VehiclesStore(this);
    this.coordinatesStore = new CoordinatesStore(this);
    this.importsStore = new ImportsStore(this);
    this.webSocketStore = new WebSocketStore(this);
    this.webSocketStore.connect('ws://localhost:8080/api/ws');
    this.userStore = new UserStore();
  }
}
